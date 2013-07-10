/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.slatec.fnlib;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.exposedapi.SLATEC;
import com.opengamma.maths.lowlevelapi.functions.FPEquals;
import com.opengamma.maths.nativewrappers.exceptions.OGNativeMathsWrapperInvalidArgumentException;

/**
 * Tests double acosh()
 */
@Test
public class DACOSHTest {

  static double[] input = new double[] {
      1.0000000000000000000000,
      1267650600228229401496703205376.0000000000000000000000,
      1606938044258990275541962092341162602522202993782792835301376.0000000000000000000000,
      2037035976334486086268445688409378161051468393665936250636140449354381299763336706183397376.0000000000000000000000,
      2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493376.0000000000000000000000,
      3273390607896141870013189696827599152216642046043064789483291368096133796404674554883270092325904157150886684127560071009217256545885393053328527589376.0000000000000000000000,
      4149515568880992958512407863691161151012446232242436899995657329690652811412908146399707048947103794288197886611300789182395151075411775307886874834113963687061181803401509523685376.0000000000000000000000,
      5260135901548373507240989882880128665550339802823173859498280903068732154297080822113666536277588451226982968856178217713019432250183803863127814770651880849955223671128444598191663757884322717271293251735781376.0000000000000000000000,
      6668014432879854274079851790721257797144758322315908160396257811764037237817632071521432200871554290742929910593433240445888801654119365080363356052330830046095157579514014558463078285911814024728965016135886601981690748037476461291163877376.0000000000000000000000,
      8452712498170643941637436558664265704301557216577944354047371344426782440907597751590676094202515006314790319892114058862117560952042968596008623655407033230534186943984081346699704282822823056848387726531379014466368452684024987821414350380272583623832617294363807973376.0000000000000000000000,
      10715086071862673209484250490600018105614048117055336074437503883703510511249361224931983788156958581275946729175531468251871452856923140435984577574698574803934567774824230985421074605062371141877954182153046474983581941267398767559165543946077062914571196477686542167660429831652624386837205668069376.0000000000000000000000 };

  static double[] expected = new double[] {0.0000000000000000000000, 70.0078652365544797930852, 139.3225832925490124125645, 208.6373013485435308211891, 277.9520194045380776515231,
      347.2667374605326244818571, 416.5814555165271144687722, 485.8961735725216612991062, 555.2108916285161512860213, 624.5256096845106412729365, 693.8403277405052449466893 };

  @Test
  public static void dacoshRangeTest() {
    double ans;
    for (int i = 0; i < input.length; i++) {
      ans = SLATEC.getInstance().dacosh(input[i]);
      assertTrue(FPEquals.fuzzyEquals(expected[i], ans));
    }
  }

  @Test(expectedExceptions = OGNativeMathsWrapperInvalidArgumentException.class)
  public static void dacoshIllegalArgTest() {
    SLATEC.getInstance().dacosh(0);
  }

}
