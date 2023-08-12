package com.briar.info;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AttackInfo {
    private int id;

    private String attackType; //攻击类型
    private Context context;
    private boolean isBlock;
    private String severity;
    private long attackTime;
    private String payload;
    private List<String> stack;




    public AttackInfo(String attackType, Context context, boolean isBlock, String severity, long attackTime, String payload, List<String> stack) {
        this.attackType = attackType;
        this.context = context;
        this.isBlock = isBlock;
        this.severity = severity;
        this.attackTime = attackTime;
        this.payload = payload;
        this.stack=stack;
    }

    public List<String> getStack() {
        return stack;
    }

    public void setStack(List<String> stack) {
        this.stack = stack;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getAttackType() {
        return attackType;
    }

    public void setAttackType(String attackType) {
        this.attackType = attackType;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public long getAttackTime() {
        return attackTime;
    }

    public void setAttackTime(long attackTime) {
        this.attackTime = attackTime;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
