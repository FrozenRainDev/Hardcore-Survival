package com.hcs.misc.accessor;

import com.hcs.main.manager.*;

public interface StatAccessor {
    ThirstManager getThirstManager();

    StaminaManager getStaminaManager();

    TemperatureManager getTemperatureManager();

    StatusManager getStatusManager();

    SanityManager getSanityManager();

    NutritionManager getNutritionManager();
}
