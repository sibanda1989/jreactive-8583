package org.jreactive.iso8583.example;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import io.netty.channel.ChannelHandlerContext;
import org.jreactive.iso8583.IsoMessageHandler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClientServerIT extends AbstractIT {

    volatile IsoMessage capturedRequest;

    @Before
    public void beforeTest() {
        server.addMessageListener(new IsoMessageHandler() {
            @Override
            public int getType() {
                return 0x200;
            }

            @Override
            public void onMessage(ChannelHandlerContext ctx, IsoMessage isoMessage) {
                capturedRequest = isoMessage;
                final IsoMessage response = server.getIsoMessageFactory().createResponse(isoMessage);
                response.setField(39, IsoType.ALPHA.value("00", 2));
                ctx.write(response);
            }
        });
    }

    @Test
    public void testConnected() throws Exception {
        assertThat(server.isStarted(), is(true));
        assertThat(client.isConnected(), is(true));

        final IsoMessage finMessage = client.getIsoMessageFactory().newMessage(0x0200);
        finMessage.setField(60, IsoType.LLLVAR.value("foo", 3));
        client.send(finMessage);

        Thread.sleep(100L);

        assertThat("fin request", capturedRequest.debugString(), equalTo(finMessage.debugString()));
    }


}
