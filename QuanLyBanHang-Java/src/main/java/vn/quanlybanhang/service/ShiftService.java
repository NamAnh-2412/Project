package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.dao.ShiftDao;
import vn.quanlybanhang.model.CaLam;
import vn.quanlybanhang.model.ShiftTotals;

public class ShiftService {

    private final ShiftDao shiftDao = new ShiftDao();

    public Optional<CaLam> openShift(int staffId) throws SQLException {
        return shiftDao.findOpenByStaff(staffId);
    }

    public ShiftTotals totals(CaLam shift) throws SQLException {
        ShiftTotals totals = shiftDao.totalsForStaff(shift.getNhanVienId(), shift.getMoLuc());
        BigDecimal opening = shift.getTienDauCa() == null ? BigDecimal.ZERO : shift.getTienDauCa();
        totals.setTienKyVong(opening.add(totals.getTienMat()));
        return totals;
    }

    public List<CaLam> recent() throws SQLException {
        return shiftDao.recent(20);
    }

    public Optional<String> open(int staffId, String staffName, BigDecimal openingCash) throws SQLException {
        if (openingCash == null || openingCash.signum() < 0) {
            return Optional.of("Tiền đầu ca không được âm.");
        }
        if (shiftDao.findOpenByStaff(staffId).isPresent()) {
            return Optional.of("Ca đang mở. Hãy kết ca trước khi mở ca mới.");
        }
        CaLam shift = new CaLam();
        shift.setNhanVienId(staffId);
        shift.setTenNhanVien(staffName);
        shift.setTienDauCa(openingCash);
        shift.setMoLuc(LocalDateTime.now());
        shiftDao.insert(shift);
        return Optional.empty();
    }

    public Optional<String> close(int staffId, BigDecimal actualCash, String note) throws SQLException {
        if (actualCash == null || actualCash.signum() < 0) {
            return Optional.of("Tiền đếm được không được âm.");
        }
        Optional<CaLam> open = shiftDao.findOpenByStaff(staffId);
        if (open.isEmpty()) {
            return Optional.of("Chưa có ca đang mở.");
        }
        CaLam shift = open.get();
        ShiftTotals totals = totals(shift);
        shift.setTienMat(totals.getTienMat());
        shift.setTienChuyenKhoan(totals.getTienChuyenKhoan());
        shift.setTienThe(totals.getTienThe());
        shift.setTienKhac(totals.getTienKhac());
        shift.setSoDon(totals.getSoDon());
        shift.setTienKyVong(totals.getTienKyVong());
        shift.setTienThucTe(actualCash);
        shift.setChenhLech(actualCash.subtract(totals.getTienKyVong()));
        shift.setGhiChu(note == null ? "" : note.trim());
        shift.setDongLuc(LocalDateTime.now());
        shiftDao.close(shift);
        return Optional.empty();
    }
}
