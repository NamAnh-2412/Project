<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Chat\Models\Conversation;
use App\Domain\JerseyStudio\Models\JerseyDesign;
use App\Domain\JerseyStudio\Services\JerseyDesignService;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class JerseyStudioController extends Controller
{
    public function __construct(private JerseyDesignService $studio) {}

    public function index(): View
    {
        return view('admin.studio.index', [
            'designs' => JerseyDesign::query()
                ->with('user')
                ->where('status', '!=', 'draft')
                ->latest('submitted_at')
                ->latest('id')
                ->paginate(20),
        ]);
    }

    public function quote(Request $request, Conversation $conversation, JerseyDesign $jerseyDesign): RedirectResponse
    {
        if ((int) $jerseyDesign->user_id !== (int) $conversation->user_id) {
            abort(404);
        }

        $data = $request->validate([
            'quote_amount' => ['required', 'numeric', 'min:1', 'max:100000000'],
            'note' => ['nullable', 'string', 'max:500'],
        ], [
            'quote_amount.required' => 'Nhập số tiền báo giá.',
            'quote_amount.min' => 'Báo giá phải lớn hơn 0.',
        ]);

        $this->studio->quote($request->user(), $jerseyDesign, (float) $data['quote_amount'], $data['note'] ?? null);

        return back()->with('success', 'Đã gửi báo giá bản phối vào hội thoại.');
    }
}
