package dev.revere.alley.library.menu.pagination;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Predicate;

@Getter
@Setter
@RequiredArgsConstructor
public class PageFilter<T> {
    private final String name;
    private final Predicate<T> predicate;
    private boolean enabled;

    /**
     * Test if the filter is enabled and the predicate passes
     * 测试过滤器是否已启用以及谓词是否通过
     *
     * @param t the object to test
     *          要测试的对象
     * @return true if the filter is enabled and the predicate passes
     *         如果过滤器已启用且谓词通过则返回true
     */
    public boolean test(T t) {
        return !enabled || predicate.test(t);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PageFilter && ((PageFilter<?>) object).getName().equals(name);
    }
}
