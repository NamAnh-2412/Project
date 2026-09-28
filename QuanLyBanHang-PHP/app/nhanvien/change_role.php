<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

header('Content-Type: application/json; charset=utf-8');

try {
    // Get JSON input
    $raw = file_get_contents('php://input');
    $data = json_decode($raw, true);
    
    if (!$data || !isset($data['id'])) {
        jsonResponse(['success' => false, 'error' => 'Invalid payload'], 400);
    }
    
    $id = (int)$data['id'];
    
    // Prevent changing own role
    $currentUserId = getCurrentUserId();
    if ($id == $currentUserId) {
        jsonResponse(['success' => false, 'error' => 'Bạn không thể đổi chức vụ của chính mình'], 403);
    }
    
    // Get current role
    $row = db_fetch_one('SELECT chuc_vu FROM nhanvien WHERE id = ? LIMIT 1', 'i', [$id]);
    
    if (!$row) {
        jsonResponse(['success' => false, 'error' => 'Employee not found'], 404);
    }
    
    $current = $row['chuc_vu'];
    
    // Toggle between roles: 'nhan_vien' and 'cua_hang_truong' (store manager)
    // Note: 'quan_ly' (admin) should not be toggled via this endpoint
    if ($current === 'cua_hang_truong') {
        $new = ROLE_STAFF; // 'nhan_vien'
    } elseif ($current === ROLE_STAFF) {
        $new = 'cua_hang_truong';
    } else {
        // Cannot change admin role or unknown role
        jsonResponse(['success' => false, 'error' => 'Cannot change this role'], 403);
    }
    
    // Update role
    $result = db_query('UPDATE nhanvien SET chuc_vu = ? WHERE id = ?', 'si', [$new, $id]);
    
    if (!$result) {
        jsonResponse(['success' => false, 'error' => 'Update failed'], 500);
    }
    
    // Log role change if table exists
    try {
        $changed_by = getCurrentUserId();
        db_query(
            'INSERT INTO role_changes (nhanvien_id, changed_by, old_role, new_role, note) VALUES (?, ?, ?, ?, ?)',
            'iisss',
            [$id, $changed_by, $current, $new, null]
        );
    } catch (Exception $e) {
        // Log table might not exist, just log error and continue
        error_log("Error logging role change: " . $e->getMessage());
    }
    
    jsonResponse([
        'success' => true,
        'id' => $id,
        'new_role' => $new
    ]);
    
} catch (Exception $e) {
    error_log("Error in change_role: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => 'Internal server error'], 500);
}
?>

