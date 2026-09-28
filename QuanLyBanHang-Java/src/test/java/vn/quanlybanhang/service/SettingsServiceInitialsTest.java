package vn.quanlybanhang.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SettingsServiceInitialsTest {

    @Test
    void normalizeLogoInitials_stripsNonAlphanumericAndCaps() {
        assertEquals("CG", SettingsService.normalizeLogoInitials("c-g!"));
        assertEquals("AB12", SettingsService.normalizeLogoInitials("ab-12**"));
    }

    @Test
    void deriveInitialsFromShopName_takesFirstLetterOfEachWord() {
        assertEquals("CG", SettingsService.deriveInitialsFromShopName("Chill Guys"));
        assertEquals("Q", SettingsService.deriveInitialsFromShopName(""));
    }
}
