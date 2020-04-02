package com.minecolonies.api.paths;

import com.minecolonies.api.IMinecoloniesAPI;

public interface IPathHandlingRegistry {

    public static IPathHandlingRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getPathHandlingRegistry();
    }

    IPathHandlingRegistry registerHandler()
}
