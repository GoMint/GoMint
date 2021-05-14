package io.gomint.server.network.handler;

import io.gomint.command.CommandOutputMessage;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.PacketCommandOutput;
import io.gomint.server.network.packet.PacketCommandRequest;
import io.gomint.server.network.type.OutputMessage;
import io.gomint.server.world.WorldAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PacketCommandRequestHandler implements PacketHandler<PacketCommandRequest> {

    @Override
    public void handle(PacketCommandRequest packet, long currentTimeMillis, PlayerConnection connection) {
        // Sanity stuff
        if (!packet.getInputCommand().startsWith("/")) {
            return;
        }

        connection.entity().dispatchCommand(packet.getInputCommand(), commandOutput -> {
            if (commandOutput != null) {

                PacketCommandOutput packetCommandOutput = new PacketCommandOutput();
                packetCommandOutput.setSuccess(commandOutput.success());
                packetCommandOutput.setOrigin(packet.getCommandOrigin().type((byte) 3));

                // Remap outputs
                List<OutputMessage> outputMessages = new ArrayList<>();
                for (CommandOutputMessage commandOutputMessage : commandOutput.messages()) {
                    outputMessages.add(new OutputMessage(commandOutputMessage.format(), commandOutputMessage.success(), commandOutputMessage.parameters()));
                }

                packetCommandOutput.setOutputs(outputMessages);
                connection.addToSendQueue(packetCommandOutput);
            }
        });
    }

}
