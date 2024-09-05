package com.mrjake.aunis.util;

public interface ITiledTexture extends ITexture {

    ITexture tile(int row, int col);
}
