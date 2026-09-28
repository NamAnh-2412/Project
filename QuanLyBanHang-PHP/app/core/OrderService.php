<?php
/**
 * Order Service - Logic tạo đơn hàng dùng chung
 */

/**
 * Tính tổng tiền từ danh sách sản phẩm trong giỏ
 */
function calculateOrderTotal(array $items) {
    $total = 0.0;
    foreach ($items as $item) {
        $gia = (float)($item['gia'] ?? 0);
        $sl = (int)($item['sl'] ?? 0);
        $total += $gia * $sl;
    }
    return $total;
}

/**
 * Chuẩn hóa phương thức thanh toán
 */
function normalizePaymentMethod($method) {
    $allowed = [PAYMENT_CASH, PAYMENT_BANK, PAYMENT_CARD, PAYMENT_APP_ONLINE];
    return in_array($method, $allowed, true) ? $method : PAYMENT_CASH;
}

/**
 * Sinh mã đơn hàng
 */
function generateOrderCode($withRandomSuffix = false) {
    $code = 'HD' . date('ymdHis');
    if ($withRandomSuffix) {
        $code .= rand(100, 999);
    }
    return $code;
}

/**
 * Tìm khách hàng theo SĐT (ưu tiên) hoặc tên
 */
function findCustomerByPhoneOrName($phone, $name) {
    if (!empty($phone)) {
        $row = db_fetch_one(
            "SELECT id FROM khachhang WHERE so_dien_thoai = ? LIMIT 1",
            's',
            [$phone]
        );
        if (!empty($row)) {
            return $row;
        }
    }

    if (!empty($name)) {
        return db_fetch_one(
            "SELECT id FROM khachhang WHERE ten_khachhang = ? LIMIT 1",
            's',
            [$name]
        );
    }

    return null;
}

/**
 * Tìm hoặc tạo khách hàng khi tạo đơn, cập nhật thống kê chi tiêu
 *
 * @return int|null ID khách hàng hoặc null nếu là khách lẻ / không có SĐT để tạo mới
 */
function resolveOrCreateCustomerForOrder($ten, $sdt, $tong) {
    global $conn;

    if (empty($ten) || $ten === 'Khách lẻ') {
        return null;
    }

    $khRow = findCustomerByPhoneOrName($sdt, $ten);

    if (!empty($khRow)) {
        $customerId = (int)$khRow['id'];
        db_query(
            "UPDATE khachhang SET tong_donhang = tong_donhang + 1, tong_tien_da_chi = tong_tien_da_chi + ? WHERE id = ?",
            'di',
            [$tong, $customerId]
        );
        recalculateCustomerRank($customerId);
        return $customerId;
    }

    if (empty($sdt)) {
        return null;
    }

    try {
        $hang = getCustomerRankFromAmount($tong);
        $inserted = db_query(
            "INSERT INTO khachhang (ten_khachhang, so_dien_thoai, tong_donhang, tong_tien_da_chi, loai_thanh_vien) VALUES (?, ?, 1, ?, ?)",
            'ssds',
            [$ten, $sdt, $tong, $hang]
        );

        if ($inserted) {
            return (int)$conn->insert_id;
        }
    } catch (Exception $e) {
        error_log("Error auto-creating customer: " . $e->getMessage());
    }

    return null;
}

/**
 * Lưu chi tiết đơn hàng và các lựa chọn sản phẩm
 */
function insertOrderLineItems($orderId, array $items) {
    global $conn;

    $stmtDetail = $conn->prepare(
        "INSERT INTO chitiet_donhang (donhang_id, sanpham_id, ten_sanpham, size, so_luong, don_gia, thanh_tien) VALUES (?, ?, ?, ?, ?, ?, ?)"
    );
    $stmtChoice = $conn->prepare(
        "INSERT INTO chitiet_donhang_lua_chon (chitiet_donhang_id, nhom_lua_chon_id, lua_chon_id, ten_nhom, ten_lua_chon, gia_tang_them) VALUES (?, ?, ?, ?, ?, ?)"
    );

    if (!$stmtDetail || !$stmtChoice) {
        throw new RuntimeException('Không thể chuẩn bị câu lệnh lưu chi tiết đơn hàng.');
    }

    foreach ($items as $item) {
        $sanphamId = (int)($item['id'] ?? 0);
        $ten = sanitize($item['ten'] ?? '', 'string');
        $size = sanitize($item['size'] ?? '', 'string');
        $sizeId = isset($item['size_id']) && !empty($item['size_id']) ? (int)$item['size_id'] : null;
        $sl = (int)($item['sl'] ?? 0);
        $gia = (float)($item['gia'] ?? 0);
        $lineTotal = $sl * $gia;

        if ($sizeId) {
            $sizeRow = db_fetch_one(
                "SELECT ten_size FROM kichthuoc_sanpham WHERE id = ? AND sanpham_id = ?",
                'ii',
                [$sizeId, $sanphamId]
            );
            if ($sizeRow) {
                $size = $sizeRow['ten_size'];
            }
        }

        $stmtDetail->bind_param('iissidd', $orderId, $sanphamId, $ten, $size, $sl, $gia, $lineTotal);
        if (!$stmtDetail->execute()) {
            $error = $stmtDetail->error;
            $stmtDetail->close();
            $stmtChoice->close();
            throw new RuntimeException('Không thể lưu sản phẩm trong đơn: ' . $error);
        }

        $detailId = $conn->insert_id;
        $choices = $item['choices'] ?? [];

        if (!empty($choices) && is_array($choices)) {
            foreach ($choices as $choice) {
                $nhomId = (int)($choice['nhom_id'] ?? 0);
                $luaChonId = (int)($choice['lua_chon_id'] ?? 0);
                $tenNhom = sanitize($choice['nhom_ten'] ?? '', 'string');
                $tenLuaChon = sanitize($choice['lua_chon_ten'] ?? '', 'string');
                $giaTang = (float)($choice['gia_tang_them'] ?? 0);

                if ($nhomId > 0 && $luaChonId > 0) {
                    $stmtChoice->bind_param('iiissd', $detailId, $nhomId, $luaChonId, $tenNhom, $tenLuaChon, $giaTang);
                    $stmtChoice->execute();
                }
            }
        }
    }

    $stmtChoice->close();
    $stmtDetail->close();
}

/**
 * Liên kết khách hàng có sẵn vào đơn (không cập nhật thống kê chi tiêu)
 */
function linkExistingCustomerToOrder($orderId, $ten, $sdt) {
    if (empty($ten) || $ten === 'Khách lẻ') {
        return;
    }

    $khRow = findCustomerByPhoneOrName($sdt, $ten);
    if (!empty($khRow)) {
        db_query(
            "UPDATE donhang SET khachhang_id = ? WHERE id = ?",
            'ii',
            [(int)$khRow['id'], $orderId]
        );
    }
}

/**
 * Lưu đơn hàng vào DB
 */
function persistOrder(array $params) {
    global $conn;

    $items = $params['items'] ?? [];
    if (empty($items) || !is_array($items)) {
        return ['success' => false, 'order_id' => null, 'error' => 'Vui lòng chọn sản phẩm.'];
    }

    $useTransaction = !isset($params['use_transaction']) || $params['use_transaction'] !== false;
    $startedTransaction = false;

    if ($useTransaction) {
        $conn->begin_transaction();
        $startedTransaction = true;
    }

    try {
        $tong = calculateOrderTotal($items);
        $ma = generateOrderCode(!empty($params['unique_code']));
        $khTen = trim($params['ten_khachhang'] ?? '') ?: 'Khách lẻ';
        $khSdt = $params['sdt_khachhang'] ?? '';
        $hinhThuc = 'tai_cho';
        $trangThai = $params['status'] ?? ORDER_STATUS_COMPLETED;
        $updateCustomerStats = !empty($params['update_customer_stats']);

        if ($trangThai === ORDER_STATUS_PENDING) {
            $pttt = PAYMENT_CASH;
        } else {
            $pttt = normalizePaymentMethod($params['phuong_thuc_thanhtoan'] ?? PAYMENT_CASH);
        }

        $stmt = $conn->prepare(
            "INSERT INTO donhang (ma_donhang, khachhang_id, tong_tien, giam_gia, thanh_tien, phuong_thuc_thanhtoan, hinh_thuc_nhan, trang_thai, sdt_khachhang, ten_khachhang) VALUES (NULLIF(?,''), NULL, ?, 0, ?, ?, ?, ?, ?, ?)"
        );

        if (!$stmt) {
            throw new RuntimeException('Không thể chuẩn bị câu lệnh tạo đơn hàng.');
        }

        $stmt->bind_param('sddsssss', $ma, $tong, $tong, $pttt, $hinhThuc, $trangThai, $khSdt, $khTen);

        if (!$stmt->execute()) {
            $error = $stmt->error;
            $stmt->close();
            throw new RuntimeException('Không thể tạo đơn hàng: ' . $error);
        }

        $orderId = (int)$conn->insert_id;
        $stmt->close();

        if ($updateCustomerStats) {
            $customerId = resolveOrCreateCustomerForOrder($khTen, $khSdt, $tong);
            if ($customerId) {
                db_query(
                    "UPDATE donhang SET khachhang_id = ? WHERE id = ?",
                    'ii',
                    [$customerId, $orderId]
                );
            }
        } else {
            linkExistingCustomerToOrder($orderId, $khTen, $khSdt);
        }

        insertOrderLineItems($orderId, $items);

        if ($startedTransaction) {
            $conn->commit();
        }

        return [
            'success' => true,
            'order_id' => $orderId,
            'error' => null,
        ];
    } catch (Exception $e) {
        if ($startedTransaction) {
            $conn->rollback();
        }

        error_log("Error in persistOrder: " . $e->getMessage());

        return [
            'success' => false,
            'order_id' => null,
            'error' => $e->getMessage(),
        ];
    }
}

/**
 * Lưu đơn chờ xử lý (đã lên món, chưa thanh toán)
 */
function createPendingOrder(array $params) {
    return persistOrder(array_merge($params, [
        'status' => ORDER_STATUS_PENDING,
        'update_customer_stats' => false,
    ]));
}

/**
 * Tạo đơn hàng đã thanh toán (hoàn thành)
 */
function createOrder(array $params) {
    return persistOrder(array_merge($params, [
        'status' => ORDER_STATUS_COMPLETED,
        'update_customer_stats' => true,
    ]));
}

/**
 * Thanh toán đơn chờ xử lý → chuyển sang hoàn thành
 */
function completePendingOrder($orderId, $paymentMethod) {
    global $conn;

    $order = db_fetch_one("SELECT * FROM donhang WHERE id = ?", 'i', [$orderId]);
    if (!$order) {
        return ['success' => false, 'error' => 'Không tìm thấy đơn hàng.'];
    }

    if ($order['trang_thai'] === ORDER_STATUS_CANCELLED) {
        return ['success' => false, 'error' => 'Đơn hàng đã bị hủy, không thể thanh toán.'];
    }

    if ($order['trang_thai'] === ORDER_STATUS_COMPLETED) {
        return ['success' => false, 'error' => 'Đơn hàng đã được thanh toán trước đó.'];
    }

    if (!canCompleteOrder($order['trang_thai'])) {
        return ['success' => false, 'error' => 'Đơn hàng không thể thanh toán ở trạng thái hiện tại.'];
    }

    $pttt = normalizePaymentMethod($paymentMethod);
    $tong = (float)($order['thanh_tien'] ?? 0);
    $khTen = $order['ten_khachhang'] ?? 'Khách lẻ';
    $khSdt = $order['sdt_khachhang'] ?? '';
    $currentStatus = $order['trang_thai'];

    $conn->begin_transaction();

    try {
        $updated = db_query(
            "UPDATE donhang SET trang_thai = ?, phuong_thuc_thanhtoan = ? WHERE id = ? AND trang_thai = ?",
            'ssis',
            [ORDER_STATUS_COMPLETED, $pttt, $orderId, $currentStatus]
        );

        if (!$updated) {
            throw new RuntimeException('Không thể cập nhật trạng thái đơn hàng.');
        }

        $customerId = resolveOrCreateCustomerForOrder($khTen, $khSdt, $tong);
        if ($customerId) {
            db_query(
                "UPDATE donhang SET khachhang_id = ? WHERE id = ?",
                'ii',
                [$customerId, $orderId]
            );
        }

        $conn->commit();

        return ['success' => true, 'error' => null];
    } catch (Exception $e) {
        $conn->rollback();
        error_log("Error in completePendingOrder: " . $e->getMessage());

        return [
            'success' => false,
            'error' => 'Có lỗi xảy ra khi thanh toán đơn hàng.',
        ];
    }
}

/**
 * Lấy chi tiết đơn hàng kèm sản phẩm và lựa chọn
 */
function getOrderDetail($orderId) {
    $order = db_fetch_one("SELECT * FROM donhang WHERE id = ?", 'i', [$orderId]);
    if (!$order) {
        return null;
    }

    $items = db_fetch_all(
        "SELECT * FROM chitiet_donhang WHERE donhang_id = ? ORDER BY id ASC",
        'i',
        [$orderId]
    );

    $totalQty = 0;
    foreach ($items as &$item) {
        $totalQty += (int)($item['so_luong'] ?? 0);
        $item['choices'] = db_fetch_all(
            "SELECT ten_nhom, ten_lua_chon, gia_tang_them
             FROM chitiet_donhang_lua_chon
             WHERE chitiet_donhang_id = ?
             ORDER BY id ASC",
            'i',
            [$item['id']]
        );
    }
    unset($item);

    return [
        'order' => $order,
        'items' => $items,
        'total_qty' => $totalQty,
        'daily_sequence' => getOrderDailySequence($orderId),
    ];
}

/**
 * Tạo nhiều đơn hàng trong một transaction
 *
 * @return array ['success' => bool, 'count' => int, 'errors' => array, 'message' => string]
 */
function createMultipleOrders(array $orders) {
    global $conn;

    if (empty($orders)) {
        return [
            'success' => false,
            'count' => 0,
            'errors' => ['Không có đơn hàng nào'],
            'message' => 'Không có đơn hàng nào',
        ];
    }

    $successCount = 0;
    $errors = [];

    $conn->begin_transaction();

    try {
        foreach ($orders as $orderData) {
            $items = $orderData['items'] ?? [];

            if (empty($items) || !is_array($items)) {
                $errors[] = 'Đơn hàng thiếu sản phẩm';
                continue;
            }

            $result = createOrder([
                'ten_khachhang' => $orderData['ten_khachhang'] ?? 'Khách lẻ',
                'sdt_khachhang' => $orderData['sdt_khachhang'] ?? '',
                'items' => $items,
                'phuong_thuc_thanhtoan' => $orderData['phuong_thuc_thanhtoan'] ?? PAYMENT_CASH,
                'unique_code' => true,
                'use_transaction' => false,
            ]);

            if ($result['success']) {
                $successCount++;
            } else {
                $errors[] = $result['error'] ?? 'Không thể tạo đơn hàng';
            }
        }

        if ($successCount > 0) {
            $conn->commit();
            return [
                'success' => true,
                'count' => $successCount,
                'errors' => $errors,
                'message' => "Đã tạo thành công {$successCount} đơn hàng",
            ];
        }

        $conn->rollback();
        return [
            'success' => false,
            'count' => 0,
            'errors' => $errors,
            'message' => 'Không thể tạo đơn hàng nào',
        ];
    } catch (Exception $e) {
        $conn->rollback();
        error_log("Error creating multiple orders: " . $e->getMessage());

        return [
            'success' => false,
            'count' => 0,
            'errors' => [$e->getMessage()],
            'message' => 'Có lỗi xảy ra: ' . $e->getMessage(),
        ];
    }
}
