<div class="header-bar">
    <h2>Trang Quản Lý Tổng</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="dashboard-cards">
    <div class="stat-card">
        <div class="label">Đơn hàng hôm nay</div>
        <div class="number"><?php echo isset($dh_today) ? $dh_today : 0; ?></div>
    </div>
    <div class="stat-card">
        <div class="label">Khách hàng trong ngày</div>
        <div class="number"><?php echo isset($kh_today) ? $kh_today : 0; ?></div>
    </div>
    <div class="stat-card">
        <div class="label">Doanh thu hôm nay</div>
        <div class="number"><?php echo isset($dt_today) ? number_format($dt_today, 0, ',', '.') : '0'; ?>₫</div>
    </div>
    <div class="products-hot">
        <h3>Sản phẩm bán chạy</h3>
        <ul class="hot-list">
        <?php if (!empty($sp_best) && is_array($sp_best)): 
            $stt = 1;
            foreach($sp_best as $row): ?>
            <li>
                <span class="stt"><?php echo $stt++; ?></span>
                <span class="sp-name"><?php echo htmlspecialchars($row['ten_sanpham'] ?? ''); ?></span>
                <span class="sp-count">(<?php echo $row['tong_sl'] ?? 0; ?> lượt bán)</span>
            </li>
        <?php endforeach; else: ?>
            <li class="nodata">Chưa có dữ liệu</li>
        <?php endif; ?>
        </ul>
    </div>
</div>
<div class="chart-card">
    <div class="chart-title">Lượng khách hàng theo ngày trong tuần</div>
    <div class="chart-placeholder">
        <?php 
        // Đảm bảo chart_bars luôn có dữ liệu
        if (!isset($chart_bars) || !is_array($chart_bars) || empty($chart_bars)) {
            // Tạo dữ liệu mặc định nếu không có
            $chart_bars = [
                ['label' => 'T2', 'value' => 0, 'height' => 30],
                ['label' => 'T3', 'value' => 0, 'height' => 30],
                ['label' => 'T4', 'value' => 0, 'height' => 30],
                ['label' => 'T5', 'value' => 0, 'height' => 30],
                ['label' => 'T6', 'value' => 0, 'height' => 30],
                ['label' => 'T7', 'value' => 0, 'height' => 30],
                ['label' => 'CN', 'value' => 0, 'height' => 30]
            ];
        }
        foreach($chart_bars as $i => $bar): 
            $height = isset($bar['height']) ? max(20, (int)$bar['height']) : 30;
            $value = isset($bar['value']) ? (int)$bar['value'] : 0;
            $label = isset($bar['label']) ? htmlspecialchars($bar['label']) : '';
        ?>
        <div class="chart-bar-wrapper">
            <div class="chart-bar" style="height:<?php echo $height; ?>px" title="<?php echo $label; ?>: <?php echo $value; ?> đơn hàng">
                <?php if ($value > 0): ?>
                <span class="bar-value"><?php echo $value; ?></span>
                <?php endif; ?>
            </div>
            <div class="bar-label"><?php echo $label; ?></div>
        </div>
        <?php endforeach; ?>
    </div>
</div>

