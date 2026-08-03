package dev.revere.alley.feature.title;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.title.model.TitleRecord;

import java.util.Map;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface TitleService extends Service {
    /**
     * Gets the map of all loaded titles.
     * 获取所有已加载头衔的映射。
     * @return A map where the key is the Kit and the value is the TitleRecord.
     *         一个映射，键为 Kit（套件），值为 TitleRecord（头衔记录）。
     */
    Map<Kit, TitleRecord> getTitles();
}
