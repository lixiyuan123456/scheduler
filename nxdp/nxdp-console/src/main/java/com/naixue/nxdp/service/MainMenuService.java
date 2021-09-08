package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.MainMenuRepository;
import com.naixue.nxdp.model.MainMenu;

@Service
public class MainMenuService {

    @Autowired
    private MainMenuRepository mainMenuRepository;

    /**
     * 游客菜单
     *
     * @return
     */
    public List<MainMenu> visitorMenus() {
        return filter(loadMenus(), -1, 1);
    }

    /**
     * 默认菜单
     *
     * @return
     */
    public List<MainMenu> defaultMenus() {
        return filter(loadMenus(), 1, 2, 3, 4, 5, 6, 7, 8);
    }

    /**
     * 管理员菜单
     *
     * @return
     */
    public List<MainMenu> adminMenus() {
        return filter(
                loadMenus(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    }

    private List<MainMenu> filter(List<MainMenu> menus, Integer... mainMenuIds) {
        List<MainMenu> filter = new ArrayList<>();
        if (CollectionUtils.isEmpty(menus)) {
            return filter;
        }
        List<Integer> menuIds = Arrays.asList(mainMenuIds);
        for (MainMenu menu : menus) {
            if (menuIds.contains(menu.getId())) {
                filter.add(menu);
            }
        }
        return filter;
    }

    private List<MainMenu> loadMenus() {
        List<MainMenu> allMenus = mainMenuRepository.findAll(new Sort(Direction.ASC, "id"));
        List<MainMenu> targetMenus = new ArrayList<>();
        menus(allMenus, targetMenus, 1);
        return targetMenus;
    }

    private void menus(
            List<MainMenu> allMenus, List<MainMenu> currentChildrenMenus, final Integer currentLevel) {
        if (CollectionUtils.isEmpty(allMenus)) {
            return;
        }
        if (currentLevel == 1) {
            for (MainMenu menu : allMenus) {
                if (menu.getLevel() == 1) {
                    currentChildrenMenus.add(menu);
                }
            }
        }
        if (CollectionUtils.isEmpty(currentChildrenMenus)) {
            return;
        }
        for (MainMenu currentChildrenMenu : currentChildrenMenus) {
            for (MainMenu menu : allMenus) {
                if (currentChildrenMenu.getId().equals(menu.getParentId())) {
                    currentChildrenMenu.getChildren().add(menu);
                }
            }
            menus(allMenus, currentChildrenMenu.getChildren(), currentChildrenMenu.getLevel() + 1);
        }
    }

  /*@Deprecated
  private List<MainMenu> menus() {
    List<MainMenu> all = mainMenuRepository.findAll(new Sort(Direction.ASC, "id"));
    if (CollectionUtils.isEmpty(all)) {
      return all;
    }
    List<MainMenu> list = new LinkedList<>();
    for (MainMenu m : all) {
      if (m.getLevel() == 1) {
        mapping(all, m);
        list.add(m);
      }
    }
    return list;
  }*/

  /*@Deprecated
  private void mapping(List<MainMenu> allMainMenus, MainMenu currentMainMenu) {
    if (!CollectionUtils.isEmpty(allMainMenus)) {
      for (MainMenu m : allMainMenus) {
        if (currentMainMenu.getId().equals(m.getParentId())) {
          currentMainMenu.getChildren().add(m);
        }
      }
    }
  }*/
}
