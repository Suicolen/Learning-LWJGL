package main.rs.buffer;

import java.util.LinkedList;


public final class Buffer {

    public static Buffer create() {
        synchronized (BUFFER_CACHE) {
            Buffer buffer = null;
            if (cache_indices > 0) {
                cache_indices--;

                buffer = (Buffer) BUFFER_CACHE.pop();
            }
            if (buffer != null) {
                buffer.pos = 0;
                return buffer;
            }
        }
        Buffer buffer = new Buffer();
        buffer.pos = 0;
        buffer.payload = new byte[5000];
        return buffer;
    }

    private Buffer() {
    }

    public Buffer(byte[] payload) {
        this.payload = payload;
        this.pos = 0;
    }

    public int get_smart_b() {
        int baseVal = 0;
        int lastVal = 0;
        while ((lastVal = get_unsigned_smart()) == 32767) {
            baseVal += 32767;
        }
        return baseVal + lastVal;
    }

    public String readNewString() {
        int i = this.pos;
        while (this.payload[this.pos++] != 0)
            ;
        return new String(this.payload, i, pos - i - 1);
    }


    public void write_byte(int value) {
        this.payload[this.pos++] = (byte) value;
    }

    public void writeShort(int value) {
        this.payload[this.pos++] = (byte) (value >> 8);
        this.payload[this.pos++] = (byte) value;
    }

    public void writeTriByte(int value) {
        this.payload[this.pos++] = (byte) (value >> 16);
        this.payload[this.pos++] = (byte) (value >> 8);
        this.payload[this.pos++] = (byte) value;
    }

    public void writeInt(int value) {
        this.payload[this.pos++] = (byte) (value >> 24);
        this.payload[this.pos++] = (byte) (value >> 16);
        this.payload[this.pos++] = (byte) (value >> 8);
        this.payload[this.pos++] = (byte) value;
    }

    public void writeLEInt(int value) {
        this.payload[this.pos++] = (byte) value;
        this.payload[this.pos++] = (byte) (value >> 8);
        this.payload[this.pos++] = (byte) (value >> 16);
        this.payload[this.pos++] = (byte) (value >> 24);
    }


    public void writeString(String text) {
        System.arraycopy(text.getBytes(), 0, this.payload, this.pos, text.length());
        this.pos += text.length();
        this.payload[this.pos++] = 10;
    }

    public void put_bytes(byte data[], int offset, int length) {
        for (int index = length; index < length + offset; index++)
            this.payload[this.pos++] = data[index];
    }

    public void put_length(int length) {
        this.payload[this.pos - length - 1] = (byte) length;
    }

    public int get_unsigned_byte() {
        return this.payload[this.pos++] & 0xff;
    }

    public byte get_signed_byte() {
        return this.payload[this.pos++];
    }

    public int get_unsigned_short() {
        this.pos += 2;
        return ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] & 0xff);
    }


    public int get_signed_short() {
        this.pos += 2;
        int value = ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] & 0xff);
        if (value > 32767)
            value -= 0x10000;

        return value;
    }

    public int get_short() {
        this.pos += 2;
        int value = ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] & 0xff);

        if (value > 60000)
            value = -65535 + value;
        return value;
    }

    public int get_24bit_int() {
        this.pos += 3;
        return ((this.payload[this.pos - 3] & 0xff) << 16) + ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] & 0xff);
    }

    public int get_int() {
        this.pos += 4;
        return ((this.payload[this.pos - 4] & 0xff) << 24) + ((this.payload[this.pos - 3] & 0xff) << 16) + ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] & 0xff);
    }

    public long get_long() {
        long msi = (long) this.get_int() & 0xffffffffL;
        long lsi = (long) this.get_int() & 0xffffffffL;
        return (msi << 32) + lsi;
    }

    public String get_string() {
        int index = this.pos;
        while (this.payload[this.pos++] != 10)
            ;
        return new String(this.payload, index, this.pos - index - 1);
    }

    public byte[] get_string_bytes() {
        int index = this.pos;
        while (this.payload[this.pos++] != 10)
            ;
        byte data[] = new byte[this.pos - index - 1];
        System.arraycopy(this.payload, index, data, index - index, this.pos - 1 - index);
        return data;
    }

    public void get_bytes(int offset, int length, byte data[]) {
        for (int index = length; index < length + offset; index++)
            data[index] = this.payload[this.pos++];
    }

    public void init_bit_access() {
        this.bit_pos = this.pos * 8;
    }

    public int get_bits(int amount) {
        int byte_offset = this.bit_pos >> 3;
        int bit_offset = 8 - (this.bit_pos & 7);
        int value = 0;
        this.bit_pos += amount;
        for (; amount > bit_offset; bit_offset = 8) {
            value += (this.payload[byte_offset++] & BIT_MASKS[bit_offset]) << amount - bit_offset;
            amount -= bit_offset;
        }
        if (amount == bit_offset)
            value += this.payload[byte_offset] & BIT_MASKS[bit_offset];
        else
            value += this.payload[byte_offset] >> bit_offset - amount & BIT_MASKS[amount];

        return value;
    }

    public void finish_bit_access() {
        this.pos = (this.bit_pos + 7) / 8;
    }

    public int get_signed_smart() {
        int value = this.payload[this.pos] & 0xff;
        if (value < 128)
            return this.get_unsigned_byte() - 64;
        else
            return this.get_unsigned_short() - 49152;
    }

    public int get_unsigned_smart() {
        int value = this.payload[this.pos] & 0xff;
        if (value < 128)
            return this.get_unsigned_byte();
        else
            return this.get_unsigned_short() - 32768;
    }


    public void put_added_byte(int value) {
        this.payload[this.pos++] = (byte) (value + 128);
    }

    public void put_negated_byte(int value) {
        this.payload[this.pos++] = (byte) (-value);
    }

    public void put_subtracted_byte(int value) {
        this.payload[this.pos++] = (byte) (128 - value);
    }

    public int get_added_byte() {
        return this.payload[this.pos++] - 128 & 0xff;
    }

    public int get_negated_byte() {
        return -this.payload[this.pos++] & 0xff;
    }

    public int get_subtracted_byte() {
        return 128 - this.payload[this.pos++] & 0xff;
    }

    public byte get_signed_added_byte() {
        return (byte) (this.payload[this.pos++] - 128);
    }

    public byte get_signed_negated_byte() {
        return (byte) -this.payload[this.pos++];
    }

    public byte get_signed_subtracted_byte() {
        return (byte) (128 - this.payload[this.pos++]);
    }

    public void put_le_short_duplicate(int value) {
        this.payload[this.pos++] = (byte) value;
        this.payload[this.pos++] = (byte) (value >> 8);
    }

    public void put_short_added(int value) {
        this.payload[this.pos++] = (byte) (value >> 8);
        this.payload[this.pos++] = (byte) (value + 128);
    }

    public void put_le_short_added(int value) {
        this.payload[this.pos++] = (byte) (value + 128);
        this.payload[this.pos++] = (byte) (value >> 8);
    }

    public int method549() {//TODO
        this.pos += 2;
        return ((this.payload[this.pos - 1] & 0xff) << 8) + (this.payload[this.pos - 2] & 0xff);
    }

    public int method550() {//TODO
        this.pos += 2;
        return ((this.payload[this.pos - 2] & 0xff) << 8) + (this.payload[this.pos - 1] - 128 & 0xff);
    }

    public int get_little_short() {
        this.pos += 2;
        return ((this.payload[this.pos - 1] & 0xff) << 8) + (this.payload[this.pos - 2] - 128 & 0xff);
    }

    public int method552() {//TODO
        this.pos += 2;
        int value = ((this.payload[this.pos - 1] & 0xff) << 8) + (this.payload[this.pos - 2] & 0xff);

        if (value > 32767)
            value -= 0x10000;
        return value;
    }

    public int method553() {//TODO
        this.pos += 2;
        int value = ((this.payload[this.pos - 1] & 0xff) << 8) + (this.payload[this.pos - 2] - 128 & 0xff);
        if (value > 32767)
            value -= 0x10000;

        return value;
    }

    public int method555() {//TODO
        this.pos += 4;
        return ((this.payload[this.pos - 2] & 0xff) << 24)
                + ((this.payload[this.pos - 1] & 0xff) << 16)
                + ((this.payload[this.pos - 4] & 0xff) << 8)
                + (this.payload[this.pos - 3] & 0xff);
    }

    public int method556() {//TODO
        this.pos += 4;
        return ((this.payload[this.pos - 3] & 0xff) << 24)
                + ((this.payload[this.pos - 4] & 0xff) << 16)
                + ((this.payload[this.pos - 1] & 0xff) << 8)
                + (this.payload[this.pos - 2] & 0xff);
    }

    public int method557() {//TODO
        this.pos += 4;
        return ((this.payload[this.pos - 2] & 255) << 8)
                + ((this.payload[this.pos - 4] & 255) << 24)
                + ((this.payload[this.pos - 3] & 255) << 16)
                + (this.payload[this.pos - 1] & 255);
    }


    public void put_reverse_data(byte data[], int length, int offset) {
        for (int index = (length + offset) - 1; index >= length; index--)
            this.payload[this.pos++] = (byte) (data[index] + 128);

    }

    public void get_reverse_data(byte data[], int offset, int length) {
        for (int index = (length + offset) - 1; index >= length; index--)
            data[index] = this.payload[this.pos++];

    }

    public byte payload[];
    public int pos;
    public int bit_pos;
    private static final int[] BIT_MASKS = {
            0, 1, 3, 7, 15, 31, 63, 127, 255,
            511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 0x1ffff, 0x3ffff,
            0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff,
            0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff, 0x1fffffff, 0x3fffffff,
            0x7fffffff, -1
    };

    private static int cache_indices;
    private static final LinkedList BUFFER_CACHE = new LinkedList();
}
