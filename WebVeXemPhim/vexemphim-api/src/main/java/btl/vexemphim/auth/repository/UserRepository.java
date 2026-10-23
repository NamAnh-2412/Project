package btl.vexemphim.auth.repository;

import btl.vexemphim.auth.entity.Role;
import btl.vexemphim.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleAndEnabled(Role role, boolean enabled);

    long countByRoleAndEnabled(Role role, boolean enabled);

    @Query("""
            select u from User u
            where (:q = '' or lower(u.username) like concat('%', :q, '%')
                or lower(coalesce(u.email, '')) like concat('%', :q, '%'))
              and (:role is null or u.role = :role)
            """)
    Page<User> search(@Param("q") String q, @Param("role") Role role, Pageable pageable);
}
