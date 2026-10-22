@if (! empty($phoi))
    <input type="hidden" name="phoi" value="1">
@endif
@if ($categoryId)
    <input type="hidden" name="category_id" value="{{ $categoryId }}">
@endif
@if ($q !== '')
    <input type="hidden" name="q" value="{{ $q }}">
@endif
