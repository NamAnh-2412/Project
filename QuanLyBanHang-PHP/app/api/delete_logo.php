<?php
// Load bootstrap first (initApi() is defined in helpers.php after bootstrap)
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication and admin role
requireApiAdmin();

// Require POST method
requireApiMethod('POST');

try {
    $logoPath = getSetting('logo_path');
    
    if (!empty($logoPath)) {
        // Delete file
        deleteImage($logoPath);
        
        // Remove from settings
        deleteSetting('logo_path');
    }
    
    jsonResponse(['success' => true]);
    
} catch (Exception $e) {
    error_log("Error deleting logo: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => 'Có lỗi xảy ra khi xóa logo'], 500);
}
?>

