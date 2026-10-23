package btl.vexemphim.catalog.service;

import btl.vexemphim.catalog.dto.RoomRequest;
import btl.vexemphim.catalog.dto.RoomResponse;
import btl.vexemphim.catalog.dto.RoomUpdateRequest;
import btl.vexemphim.catalog.dto.SeatLayoutResponse;
import btl.vexemphim.catalog.entity.Room;
import btl.vexemphim.catalog.entity.Seat;
import btl.vexemphim.catalog.repository.RoomRepository;
import btl.vexemphim.catalog.repository.SeatRepository;
import btl.vexemphim.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;

    public RoomService(RoomRepository roomRepository, SeatRepository seatRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
    }

    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream()
                .map(RoomService::toResponse)
                .toList();
    }

    @Transactional
    public RoomResponse create(RoomRequest req) {
        String name = req.name().trim();
        if (roomRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "ROOM_NAME_TAKEN", "Tên phòng đã tồn tại");
        }
        int vip = req.vipRowCount();
        if (vip > req.rowCount()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Số hàng VIP không được lớn hơn số hàng");
        }
        Room room = new Room();
        room.setName(name);
        room.setRowCount(req.rowCount());
        room.setSeatsPerRow(req.seatsPerRow());
        room.setStatus("ACTIVE");
        for (int row = 1; row <= req.rowCount(); row++) {
            String label = String.valueOf((char) ('A' + row - 1));
            boolean vipRow = row > req.rowCount() - vip;
            for (int n = 1; n <= req.seatsPerRow(); n++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setRowLabel(label);
                seat.setNumber(n);
                seat.setType(vipRow ? "VIP" : "STANDARD");
                room.getSeats().add(seat);
            }
        }
        roomRepository.saveAndFlush(room);
        if (pairLastStandardRow(room.getSeats())) {
            seatRepository.saveAll(room.getSeats());
        }
        return toResponse(room);
    }

    /** Hàng STANDARD cuối cùng thành ghế đôi. Hàng VIP giữ nguyên. */
    @Transactional
    public void pairRoomsWithoutCouples() {
        for (Room room : roomRepository.findAll()) {
            List<Seat> seats = seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(room.getId());
            boolean hasCouple = seats.stream().anyMatch(s -> "COUPLE".equalsIgnoreCase(s.getType()));
            if (hasCouple) {
                continue;
            }
            if (pairLastStandardRow(seats)) {
                seatRepository.saveAll(seats);
            }
        }
    }

    private static boolean pairLastStandardRow(List<Seat> seats) {
        Map<String, List<Seat>> byRow = new LinkedHashMap<>();
        for (Seat seat : seats) {
            byRow.computeIfAbsent(seat.getRowLabel(), key -> new ArrayList<>()).add(seat);
        }
        String chosen = null;
        for (Map.Entry<String, List<Seat>> entry : byRow.entrySet()) {
            boolean allStandard = entry.getValue().stream().allMatch(s -> "STANDARD".equalsIgnoreCase(s.getType()));
            if (allStandard && entry.getValue().size() >= 2) {
                chosen = entry.getKey();
            }
        }
        if (chosen == null) {
            return false;
        }
        List<Seat> row = byRow.get(chosen);
        row.sort(Comparator.comparingInt(Seat::getNumber));
        boolean changed = false;
        for (int i = 0; i + 1 < row.size(); i += 2) {
            Seat left = row.get(i);
            Seat right = row.get(i + 1);
            if (left.getId() == null || right.getId() == null || left.getNumber() + 1 != right.getNumber()) {
                continue;
            }
            left.setType("COUPLE");
            right.setType("COUPLE");
            left.setPairSeatId(right.getId());
            right.setPairSeatId(left.getId());
            changed = true;
        }
        return changed;
    }

    @Transactional
    public RoomResponse update(Long id, RoomUpdateRequest req) {
        Room room = get(id);
        String name = req.name() == null ? null : req.name().trim();
        String status = req.status() == null ? null : req.status().trim().toUpperCase();
        if ((name == null || name.isEmpty()) && (status == null || status.isEmpty())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Nhập tên hoặc trạng thái phòng");
        }
        if (name != null && !name.isEmpty()) {
            if (roomRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new ApiException(HttpStatus.CONFLICT, "ROOM_NAME_TAKEN", "Tên phòng đã tồn tại");
            }
            room.setName(name);
        }
        if (status != null && !status.isEmpty()) {
            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Trạng thái phải là Dùng hoặc Dừng");
            }
            room.setStatus(status);
        }
        return toResponse(room);
    }

    private static RoomResponse toResponse(Room r) {
        return new RoomResponse(r.getId(), r.getName(), r.getRowCount(), r.getSeatsPerRow(), r.getStatus());
    }

    public Room get(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy phòng"));
    }

    public List<SeatLayoutResponse> seats(Long roomId) {
        get(roomId);
        return seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(roomId).stream()
                .map(s -> new SeatLayoutResponse(s.getId(), s.label(), s.getRowLabel(), s.getNumber(), s.getType()))
                .toList();
    }

    public List<Seat> seatsOf(Long roomId) {
        return seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(roomId);
    }
}
