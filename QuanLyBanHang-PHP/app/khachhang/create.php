<?php
require_once '../core/bootstrap.php';

requireLogin();

setFlashMessage('info', 'Khách hàng được tạo tự động khi đặt hàng. Vui lòng tạo đơn hàng để thêm khách mới.');
redirect('index.php');
?>
