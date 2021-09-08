package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.MainMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainMenuRepository extends JpaRepository<MainMenu, Integer> {

    MainMenu findByParentId(Integer parentId);
}
