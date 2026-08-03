package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Role;

public interface RoleService {
    List<Role> findAll();
    Optional<Role> findById(Integer id);
    Role save(Role role);
    void deleteById(Integer id);
}
