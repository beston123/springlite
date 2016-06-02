package org.springlite.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface InputStreamSource {

    InputStream getInputStream() throws IOException;
}
