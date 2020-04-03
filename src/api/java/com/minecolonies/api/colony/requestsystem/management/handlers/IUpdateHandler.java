package com.minecolonies.api.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;

public interface IUpdateHandler
{
    IRequestManager getManager();

    void handleUpdate();

    int getCurrentVersion();
}
