package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
