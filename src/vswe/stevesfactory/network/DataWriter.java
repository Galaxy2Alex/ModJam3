package vswe.stevesfactory.network;


import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.inventory.ICrafting;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.interfaces.ContainerManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataWriter {
    private ByteArrayOutputStream stream;
    private int byteBuffer;
    private int bitCountBuffer;

    public DataWriter() {
       stream = new ByteArrayOutputStream();
    }

    public void writeByte(int data) {
        writeData(data, 8);
    }

    public void writeBoolean(boolean data) {
        writeData(data ? 1 : 0, DataBitHelper.BOOLEAN);
    }

    public void writeData(int data, DataBitHelper bitCount) {
        writeData(data, bitCount.getBitCount());
    }

    public void writeData(int data, int bitCount) {
        int mask = (int)Math.pow(2, bitCount) - 1;

        data &= mask;

        while (true) {
            if (bitCountBuffer + bitCount >= 8) {
                int bitsToAdd = 8 - bitCountBuffer;
                int addMask = (int)Math.pow(2, bitsToAdd) - 1;
                int addData = data & addMask;
                data >>>= bitsToAdd;
                addData <<= bitCountBuffer;
                byteBuffer |= addData;

                stream.write(byteBuffer);

                byteBuffer = 0;
                bitCount -= bitsToAdd;
                bitCountBuffer = 0;
            }else{
                byteBuffer |= data << bitCountBuffer;
                bitCountBuffer += bitCount;
                break;
            }
        }
    }


    void sendPlayerPacket(Player player){
        if (bitCountBuffer > 0) {
            stream.write(byteBuffer);
        }

        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(StevesFactoryManager.CHANNEL, stream.toByteArray()), player);
    }

    void sendServerPacket() {
        if (bitCountBuffer > 0) {
            stream.write(byteBuffer);
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(StevesFactoryManager.CHANNEL, stream.toByteArray()));
    }
    
    void sendPlayerPackets(ContainerManager container) {
        if (bitCountBuffer > 0) {
            stream.write(byteBuffer);
        }

        for (ICrafting crafting : container.getCrafters()) {
            if (crafting instanceof Player) {
                Player player = (Player)crafting;
                PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(StevesFactoryManager.CHANNEL, stream.toByteArray()), player);
            }
        }
    }

    void sendGlobalPlayerPacket() {
        if (bitCountBuffer > 0) {
            stream.write(byteBuffer);
        }

        PacketDispatcher.sendPacketToAllPlayers(PacketDispatcher.getPacket(StevesFactoryManager.CHANNEL, stream.toByteArray()));
    }

    void close() {
        try {
            stream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



}