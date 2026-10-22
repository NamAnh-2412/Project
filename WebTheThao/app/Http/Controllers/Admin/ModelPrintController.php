<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Chat\Models\Conversation;
use App\Domain\ThreeDPrint\Models\ModelPrintRequest;
use App\Domain\ThreeDPrint\Services\ModelPrintService;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;

class ModelPrintController extends Controller
{
    public function __construct(private ModelPrintService $models) {}

    public function quote(Request $request, Conversation $conversation, ModelPrintRequest $modelRequest): RedirectResponse
    {
        if ((int) $modelRequest->user_id !== (int) $conversation->user_id) {
            abort(404);
        }

        $data = $request->validate([
            'quote_amount' => ['required', 'numeric', 'min:1', 'max:100000000'],
            'note' => ['nullable', 'string', 'max:500'],
        ], [
            'quote_amount.required' => 'Nhập số tiền báo giá.',
            'quote_amount.min' => 'Báo giá phải lớn hơn 0.',
        ]);

        $this->models->quote($request->user(), $modelRequest, (float) $data['quote_amount'], $data['note'] ?? null);

        return redirect()
            ->route('admin.messages.show', $conversation)
            ->with('success', 'Đã gửi báo giá in 3D vào hội thoại.');
    }
}
