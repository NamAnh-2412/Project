package btl.vexemphim.booking.event;

import java.util.List;

/** Ghế vừa đổi sau khi transaction giữ/nhả/bán đã commit. WebSocket chỉ đọc sự kiện này. */
public record SeatChangedEvent(Long showtimeId, List<SeatPatch> updatedSeats) {
    public record SeatPatch(Long seatId, String status) {
    }
}
