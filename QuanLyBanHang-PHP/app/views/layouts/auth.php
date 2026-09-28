<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><?php echo isset($page_title) ? htmlspecialchars($page_title) . ' - ' : ''; ?>CHILL GUYS</title>
  <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/auth.css">
  <?php if (isset($page_css) && is_array($page_css)): ?>
    <?php foreach ($page_css as $css): ?>
      <link rel="stylesheet" href="<?php echo getBaseUrl(); ?>assets/css/<?php echo htmlspecialchars($css); ?>">
    <?php endforeach; ?>
  <?php endif; ?>
</head>
<body>
  <?php echo $content; ?>
  <script>
    // Set base path for JavaScript modules
    window.APP_BASE_PATH = '<?php echo getBaseUrl(); ?>';
  </script>
  <script src="<?php echo getBaseUrl(); ?>js/main.js"></script>
  <?php if (isset($page_js) && is_array($page_js)): ?>
    <?php foreach ($page_js as $js): ?>
      <script src="<?php echo getBaseUrl(); ?>js/<?php echo htmlspecialchars($js); ?>"></script>
    <?php endforeach; ?>
  <?php endif; ?>
</body>
</html>

