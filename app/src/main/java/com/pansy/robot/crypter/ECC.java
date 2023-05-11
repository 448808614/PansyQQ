package com.pansy.robot.crypter;

public class ECC {
    public int ec_key;//EC_KEY*
    public int ec_group;//EC_GROUP*
    public int ec_point;//EC_POINT*
    public byte[] pub_key;
    public byte[] sha_key;
    public byte[] sha_key_new;
}
