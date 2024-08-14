using MessagePack;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;

[MessagePackObject]
public class CombatResultMsg
{
    [Key(0)]
    public bool success;
    [Key(1)]
    public long winPlayerID;
    [Key(2)]
    public SimDataOfCommander leftCommanderData;
    [Key(3)]
    public List<GameVersusPlayerCardState> leftHandCards;
}

[MessagePackObject]
public class SimDataOfCommander
{
    [Key(0)]
    public long playerID;
    [Key(1)]
    public string playerName;
}

[MessagePackObject]
public class GameVersusPlayerCardState
{
    [Key(0)]
    public int cardID;
    [Key(1)]
    public List<GameVersusBuffState> unitCardBuffs = new List<GameVersusBuffState>();
}
[MessagePackObject]
public class GameVersusBuffState
{
    [Key(0)]
    public int buffID;
    [Key(1)]
    public string buffName;
}
