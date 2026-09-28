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
    // Check if file was uploaded
    if (!isset($_FILES['logo'])) {
        jsonResponse(['success' => false, 'error' => 'Không có file được upload'], 400);
    }
    
    // Check upload error
    $uploadError = $_FILES['logo']['error'];
    if ($uploadError !== UPLOAD_ERR_OK) {
        $errorMessages = [
            UPLOAD_ERR_INI_SIZE => 'File vượt quá kích thước tối đa cho phép (upload_max_filesize)',
            UPLOAD_ERR_FORM_SIZE => 'File vượt quá kích thước tối đa trong form',
            UPLOAD_ERR_PARTIAL => 'File chỉ được upload một phần',
            UPLOAD_ERR_NO_FILE => 'Không có file được upload',
            UPLOAD_ERR_NO_TMP_DIR => 'Thiếu thư mục tạm',
            UPLOAD_ERR_CANT_WRITE => 'Không thể ghi file',
            UPLOAD_ERR_EXTENSION => 'Upload bị chặn bởi extension'
        ];
        $errorMsg = $errorMessages[$uploadError] ?? 'Lỗi upload không xác định (Code: ' . $uploadError . ')';
        jsonResponse(['success' => false, 'error' => $errorMsg], 400);
    }
    
    error_log("Starting logo upload...");
    
    // Upload logo to uploads/images/shop directory
    // Parameters: fieldName, uploadDir, allowedTypes, maxSize(2MB), autoCrop(true), cropSize(800px), maxWidth(1200px), maxHeight(1200px), quality(85)
    try {
        $uploadResult = uploadImage('logo', 'uploads/images/shop', ['image/jpeg', 'image/png', 'image/gif', 'image/webp'], 2097152, true, 800, 1200, 1200, 85);
    } catch (Exception $e) {
        error_log("Exception in uploadImage: " . $e->getMessage());
        error_log("Stack trace: " . $e->getTraceAsString());
        jsonResponse(['success' => false, 'error' => 'Lỗi khi xử lý ảnh: ' . $e->getMessage()], 500);
    } catch (Error $e) {
        error_log("Fatal error in uploadImage: " . $e->getMessage());
        error_log("Stack trace: " . $e->getTraceAsString());
        jsonResponse(['success' => false, 'error' => 'Lỗi nghiêm trọng khi xử lý ảnh'], 500);
    }
    
    if (!$uploadResult['success']) {
        $errorMsg = $uploadResult['error'] ?? 'Có lỗi xảy ra khi upload logo';
        error_log("Upload logo error: " . $errorMsg);
        jsonResponse(['success' => false, 'error' => $errorMsg], 400);
    }
    
    error_log("Logo uploaded successfully: " . $uploadResult['path']);
    
    // Delete old logo if exists
    try {
        $oldLogo = getSetting('logo_path');
        if (!empty($oldLogo)) {
            $oldLogoPath = defined('ROOT_PATH') ? ROOT_PATH . '/' . $oldLogo : dirname(dirname(__DIR__)) . '/' . $oldLogo;
            if (file_exists($oldLogoPath)) {
                deleteImage($oldLogo);
                error_log("Old logo deleted: " . $oldLogo);
            }
        }
    } catch (Exception $e) {
        error_log("Warning: Failed to delete old logo: " . $e->getMessage());
        // Continue anyway, not critical
    }
    
    // Save new logo path
    try {
        $saveResult = setSetting('logo_path', $uploadResult['path']);
        if (!$saveResult) {
            error_log("Warning: Failed to save logo_path to database");
            // Still return success, file is uploaded
        }
    } catch (Exception $e) {
        error_log("Error saving logo_path: " . $e->getMessage());
        // Still return success, file is uploaded
    }
    
    // Get base URL safely
    try {
        $baseUrl = getBaseUrl();
    } catch (Exception $e) {
        error_log("Warning: Failed to get base URL: " . $e->getMessage());
        $baseUrl = '/';
    }
    
    jsonResponse([
        'success' => true,
        'path' => $uploadResult['path'],
        'url' => $baseUrl . $uploadResult['path']
    ]);
    
} catch (Exception $e) {
    ob_clean();
    error_log("Error uploading logo: " . $e->getMessage());
    error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
    error_log("Stack trace: " . $e->getTraceAsString());
    jsonResponse(['success' => false, 'error' => 'Có lỗi xảy ra khi upload logo: ' . $e->getMessage()], 500);
} catch (Error $e) {
    ob_clean();
    error_log("Fatal error uploading logo: " . $e->getMessage());
    error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
    error_log("Stack trace: " . $e->getTraceAsString());
    jsonResponse(['success' => false, 'error' => 'Có lỗi nghiêm trọng khi upload logo: ' . $e->getMessage()], 500);
}
?>

