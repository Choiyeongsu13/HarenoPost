package com.harenopost.entity;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 우체통 장식. 테마마다 슬롯(top/front/side)별 1개씩 3개 */
@Getter
@RequiredArgsConstructor
public enum Deco {

	KAGAMI("鏡餅", Slot.TOP, Theme.SHOGATSU),
	SHIMEKAZARI("しめ飾り", Slot.FRONT, Theme.SHOGATSU),
	KADOMATSU("門松", Slot.SIDE, Theme.SHOGATSU),

	HAT("とんがり帽子", Slot.TOP, Theme.BIRTHDAY),
	RIBBON("リボン", Slot.FRONT, Theme.BIRTHDAY),
	BALLOON("風船", Slot.SIDE, Theme.BIRTHDAY),

	SANTA("サンタ帽", Slot.TOP, Theme.CHRISTMAS),
	WREATH("リース", Slot.FRONT, Theme.CHRISTMAS),
	PRESENT("プレゼント", Slot.SIDE, Theme.CHRISTMAS);

	/** 슬롯 순서 = 선언 순서 (TOP, FRONT, SIDE) */
	public enum Slot {
		TOP, FRONT, SIDE
	}

	private final String label;
	private final Slot slot;
	private final Theme theme;

	/** 테마의 장식 3개 (슬롯 순) */
	public static List<Deco> of(Theme theme) {
		return Arrays.stream(values()).filter(d -> d.theme == theme).toList();
	}

	/** 코드 → Deco. 없는 코드면 null */
	public static Deco fromCode(String code) {
		for (Deco d : values()) {
			if (d.name().equals(code)) {
				return d;
			}
		}
		return null;
	}
}
