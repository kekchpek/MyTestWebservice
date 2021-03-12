import com.kekchpek.mytestwebservice.GetPostService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectableChannel;

public class GetPostServiceTests {

    @Test
    public void testStartup1() throws IOException, NoSuchFieldException, IllegalAccessException {
        SelectableChannel mockChannel = Mockito.mock(SelectableChannel.class);
        GetPostService service = GetPostService.createNew(mockChannel);
        service.startup();
        Field field = GetPostService.class.getDeclaredField("selector");
        field.setAccessible(true);
        Object selector = field.get(service);
        Assert.assertNotNull(selector);
    }

}
