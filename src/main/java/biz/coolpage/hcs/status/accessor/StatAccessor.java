package biz.coolpage.hcs.status.accessor;

import biz.coolpage.hcs.status.manager.*;

public interface StatAccessor {
    ThirstManager getThirstManager();

    StaminaManager getStaminaManager();

    TemperatureManager getTemperatureManager();

    StatusManager getStatusManager();

    SanityManager getSanityManager();

    NutritionManager getNutritionManager();

    WetnessManager getWetnessManager();

    InjuryManager getInjuryManager();

    MoodManager getMoodManager();

    DiseaseManager getDiseaseManager();
}
