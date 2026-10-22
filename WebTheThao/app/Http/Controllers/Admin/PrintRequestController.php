<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Chat\Models\Conversation;
use App\Domain\CustomPrint\Models\PrintRequest;
use App\Domain\CustomPrint\Services\PrintRequestService;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;

class PrintRequestController extends Controller
{
    public function __construct(private PrintRequestService $prints) {}

    public function quote(Request $request, Conversation $conversation, PrintRequest $printRequest): RedirectResponse
    {
        if ((int) $printRequest->user_id !== (int) $conversation->user_id) {
            abort(404);
        }

        $data = $request->validate([
            'quote_amount' => ['required', 'numeric', 'min:1', 'max:100000000'],
            'note' => ['nullable', 'string', 'max:500'],
        ], [
            'quote_amount.required' => 'Nhập số tiền báo giá.',
            'quote_amount.min' => 'Báo giá phải lớn hơn 0.',
        ]);

        $this->prints->quote(
            $request->user(),
            $printRequest,
            (float) $data['quote_amount'],
            $data['note'] ?? null,
        );

        return redirect()
            ->route('admin.messages.show', $conversation)
            ->with('success', 'Đã gửi báo giá vào hội thoại.');
    }
}
