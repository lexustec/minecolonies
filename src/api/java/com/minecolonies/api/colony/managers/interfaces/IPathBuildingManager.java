package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

public interface IPathBuildingManager {
    void onCitizenTick(AbstractEntityCitizen citizen);

    void onColonyTick(IColony colony);
}
