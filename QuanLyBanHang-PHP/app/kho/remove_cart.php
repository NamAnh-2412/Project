<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

header('Content-Type: application/json; charset=utf-8');

try {
    // Get JSON input
    $body = json_decode(file_get_contents('php://input'), true);
    
    if (!$body) {
        jsonResponse(['success' => false, 'error' => 'Dữ liệu không hợp lệ'], 400);
    }
    
    $id = (int)($body['id'] ?? 0);
    
    if ($id <= 0) {
        jsonResponse(['success' => false, 'error' => 'Dữ liệu không hợp lệ'], 400);
    }
    
    // Initialize cart if not exists
    if (!isset($_SESSION['kho_cart'])) {
        $_SESSION['kho_cart'] = [];
    }
    
    // Remove item from cart
    $new = [];
    foreach ($_SESSION['kho_cart'] as $c) {
        if ($c['id'] != $id) {
            $new[] = $c;
        }
    }
    $_SESSION['kho_cart'] = $new;
    
    jsonResponse(['success' => true, 'cart' => $_SESSION['kho_cart']]);
    
} catch (Exception $e) {
    error_log("Error in kho/remove_cart: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => 'Internal server error'], 500);
}
?>

