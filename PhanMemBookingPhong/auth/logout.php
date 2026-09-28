<?php
require_once '../config/config.php';

// Xóa tất cả session
unset($_SESSION['user_id']);
unset($_SESSION['admin_id']);
unset($_SESSION['user']);
unset($_SESSION['admin']);
unset($_SESSION['email']);
unset($_SESSION['vai_tro']);
unset($_SESSION['admin_ten_dang_nhap']);

session_destroy();
header('Location: ' . BASE_URL . 'auth/login.php');
exit;
?>

