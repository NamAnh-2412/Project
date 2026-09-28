<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><?php echo isset($page_title) ? htmlspecialchars($page_title) . ' - ' : ''; ?>CHILL GUYS</title>
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/style.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/layout.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/loading.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/animations.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/empty-states.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/forms.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/tables.css">
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/micro-interactions.css">
  <?php if (isset($page_css) && is_array($page_css)): ?>
    <?php foreach ($page_css as $css): ?>
      <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/<?php echo htmlspecialchars($css); ?>">
    <?php endforeach; ?>
  <?php endif; ?>
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/dark-mode.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css"/>
</head>
<body<?php echo isset($body_class) ? ' class="' . htmlspecialchars($body_class) . '"' : ''; ?>>
<div class="app-wrapper">
  <?php 
  // Calculate path to sidebar from view location
  $sidebar_path = __DIR__ . '/../../core/sidebar.php';
  if (file_exists($sidebar_path)) {
      include $sidebar_path;
  }
  ?>
  <div class="main-content">
    <div class="main-inner">
      <?php if (isset($show_header) && $show_header !== false): ?>
      <div class="header-bar">
        <h2><?php echo isset($page_title) ? htmlspecialchars($page_title) : ''; ?></h2>
        <div class="accbox">
          <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
          <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
        </div>
      </div>
      <?php endif; ?>
      
      <?php echo $content; ?>
    </div>
  </div>
</div>
<script>
  // Set base path for JavaScript modules
  window.APP_BASE_PATH = '<?php echo getBaseUrl(); ?>';
</script>
<script src="<?php echo getBaseUrl(); ?>js/main.js"></script>
<script src="<?php echo getBaseUrl(); ?>js/utils/loading.js"></script>
<?php if (isset($page_js) && is_array($page_js)): ?>
  <?php foreach ($page_js as $js): ?>
    <script src="<?php echo getBaseUrl(); ?>js/<?php echo htmlspecialchars($js); ?>"></script>
  <?php endforeach; ?>
<?php endif; ?>
</body>
</html>

