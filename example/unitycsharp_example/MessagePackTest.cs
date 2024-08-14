using MessagePack;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.ConstrainedExecution;
using System.Text;
using UnityEngine;

public class MessagePackTest : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {

        GameVersusBuffState gameVersusBuffState = new GameVersusBuffState();
        gameVersusBuffState.buffID = 1;
        gameVersusBuffState.buffName = "1buff";
        GameVersusBuffState gameVersusBuffState2 = new GameVersusBuffState();
        gameVersusBuffState.buffID = 2;
        gameVersusBuffState.buffName = "2buff";

        SimDataOfCommander simDataOfCommander = new SimDataOfCommander();
        simDataOfCommander.playerID = 1;
        simDataOfCommander.playerName = "zhang san";

        List<GameVersusPlayerCardState> gameVersusPlayerCardStates = new List<GameVersusPlayerCardState>();
        GameVersusPlayerCardState gameVersusPlayerCardState1 = new GameVersusPlayerCardState();
        gameVersusPlayerCardState1.cardID = 1;
        gameVersusPlayerCardState1.unitCardBuffs = new List<GameVersusBuffState>();
        gameVersusPlayerCardState1.unitCardBuffs.Add(gameVersusBuffState);
        gameVersusPlayerCardState1.unitCardBuffs.Add(gameVersusBuffState2);
        gameVersusPlayerCardStates.Add(gameVersusPlayerCardState1);


        CombatResultMsg combatResultMsg = new CombatResultMsg();
        combatResultMsg.success = true;
        combatResultMsg.winPlayerID = 1;
        combatResultMsg.leftCommanderData = simDataOfCommander;
        combatResultMsg.leftHandCards = gameVersusPlayerCardStates;

        byte[] bin = MessagePackSerializer.Serialize<CombatResultMsg>(combatResultMsg, MessagePackSerializerOptions.Standard);
        Debug.Log(bin.Length);
        Debug.Log(ToHexStrFromBytes(bin));
        CombatResultMsg person = MessagePackSerializer.Deserialize<CombatResultMsg>(bin);


        //List<int> skillList = new List<int>() { 1, 2, 3, 4, 5 };
        //Person per1 = new Person("zhang san", 18, skillList, null);
        //Person per2 = new Person("li si", 19, skillList, null);

        //List<Person> people = new List<Person>() { per1, per2 };

        //Person per = new Person("Ram", 20, skillList, people);
        ////Person per = new Person("Ram", 20);

        ////Debug.Log(per.toString());
        //byte[] bin = MessagePackSerializer.Serialize<Person>(per, MessagePackSerializerOptions.Standard);

        //Debug.Log(bin.Length);
        //////打印
        ////foreach (byte b in bin)
        ////{
        ////    Debug.Log(b);
        ////}

        ////以16进制打印序列化后的数据
        //Debug.Log(ToHexStrFromBytes(bin));


        //Person person = MessagePackSerializer.Deserialize<Person>(bin);
        ////Debug.Log(person.toString());
    }

    public string ToHexStrFromBytes(byte[] bytes)
    {
        StringBuilder strBuilder = new StringBuilder();
        foreach (byte b in bytes)
        {
            strBuilder.Append(b.ToString("X2"));
            strBuilder.Append("-");
        }
        return strBuilder.ToString();

    }
}
