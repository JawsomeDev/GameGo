package com.gamego.domain.room.dto;


import com.gamego.domain.game.dto.GameTagResp;
import lombok.Data;

import java.util.List;

@Data
public class RoomResp {

    private String path;

    private String title;

    private String shortDescription;

    private String longDescription;

    private boolean useBanner;

    private String image;

    private boolean active;
    private boolean closed;
    private boolean recruiting;

    private boolean joinAble;

    private int memberCount;

    // 매니저 이상의 권한을 갖고있는지 여부
    private boolean managerOrMaster;

    private boolean user;

    private boolean manager;

    private boolean master;

    private String timePreference;

    private List<GameTagResp> games;

    private List<RoomMemberResp> members;

}
