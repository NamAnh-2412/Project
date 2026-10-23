package btl.vexemphim.catalog;

import btl.vexemphim.catalog.service.RoomService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CoupleSeatLinker implements ApplicationRunner {

    private final RoomService roomService;

    public CoupleSeatLinker(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void run(ApplicationArguments args) {
        roomService.pairRoomsWithoutCouples();
    }
}
