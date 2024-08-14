import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Main {

  public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException,
      InvocationTargetException, NoSuchMethodException, ClassNotFoundException, ClassNotFoundException {

    GameVersusBuffState buffState = new GameVersusBuffState();
    buffState.buffID = 1;
    buffState.buffName = "1buff";
    GameVersusBuffState buffState2 = new GameVersusBuffState();
    buffState.buffID = 2;
    buffState.buffName = "2buff";

    SimDataOfCommander commanderData = new SimDataOfCommander();
    commanderData.playerID = 1;
    commanderData.playerName = "zhang san";

    ArrayList<GameVersusPlayerCardState> playerCardStates = new ArrayList<GameVersusPlayerCardState>();
    GameVersusPlayerCardState playerCardState = new GameVersusPlayerCardState();
    playerCardState.cardID = 1;
    playerCardState.unitCardBuffs = new ArrayList<GameVersusBuffState>();
    playerCardState.unitCardBuffs.add(buffState);
    playerCardState.unitCardBuffs.add(buffState2);
    playerCardStates.add(playerCardState);

    CombatResultMsg combatResultMsg = new CombatResultMsg();
    combatResultMsg.success = true;
    combatResultMsg.winPlayerID = 1;
    combatResultMsg.leftCommanderData = commanderData;
    combatResultMsg.leftHandCards = playerCardStates;

    byte[] serializedPerson = MessagePackSerializer.serialize(combatResultMsg);
    System.out.println(serializedPerson);
    System.out.println("序列化之后的长度是：" + serializedPerson.length);

    // 输出序列化的字节数组
    System.out.println("Serialized data: " + java.util.Arrays.toString(serializedPerson));

    // 输出序列化的字节数组，以16进制格式
    System.out.println("Serialized data (hex): " + bytesToHex(serializedPerson));

    // 使用MessagePackSerializer类进行反序列化
    CombatResultMsg deserializedPerson = MessagePackSerializer.deserialize(serializedPerson, CombatResultMsg.class);

    // 输出反序列化后的Person对象
    System.out.println("Deserialized Person: " + deserializedPerson);

    return;

    // // 创建并初始化Person对象
    // ArrayList<Integer> skill = new ArrayList<Integer>();
    // skill.add(1);
    // skill.add(2);
    // skill.add(3);
    // skill.add(4);
    // skill.add(5);
    // Person personf1 = new Person("zhang san", 18, skill, null);
    // Person personf2 = new Person("li si", 19, skill, null);
    // ArrayList<Person> friendss = new ArrayList<Person>();
    // friendss.add(personf1);
    // friendss.add(personf2);

    // Person per = new Person("Ram", 20, skill, friendss);

    // // 使用MessagePackSerializer类进行序列化
    // byte[] serializedPerson = MessagePackSerializer.serialize(per);
    // ;

    // System.out.println("序列化之后的长度是："+serializedPerson.length);

    // // 输出序列化的字节数组
    // System.out.println("Serialized data: " +
    // java.util.Arrays.toString(serializedPerson));

    // // 输出序列化的字节数组，以16进制格式
    // System.out.println("Serialized data (hex): " + bytesToHex(serializedPerson));

    // // 使用MessagePackSerializer类进行反序列化
    // Person deserializedPerson =
    // MessagePackSerializer.deserialize(serializedPerson, Person.class);

    // // 输出反序列化后的Person对象
    // System.out.println("Deserialized Person: " + deserializedPerson);

    // //使用Json序列化对象
    // String json = JsonSerializer.serialize(per);
    // System.out.println("Serialized data: " + json);
  }

  // byte转16进制字符串
  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
      sb.append("-");
    }
    return sb.toString();
  }
}
