package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class KauppaTest {

    private Pankki pankki;
    private Viitegeneraattori viite;
    private Varasto varasto;
    private Kauppa kauppa;

    @Before
    public void setup() {
        pankki = mock(Pankki.class);
        viite = mock(Viitegeneraattori.class);
        varasto = mock(Varasto.class);
        kauppa = new Kauppa(varasto, pankki, viite);
    }

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        when(viite.uusi()).thenReturn(42);

        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());
    }

    @Test
    public void tilisiirtoOikeillaParametreillaYksiTuote() {
        when(viite.uusi()).thenReturn(100);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(100), eq("12345"), eq("33333-44455"), eq(5));
    }

    @Test
    public void tilisiirtoOikeillaParametreillaKaksiEriTuotetta() {
        when(viite.uusi()).thenReturn(101);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(100);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(101), eq("12345"), eq("33333-44455"), eq(12));
    }

    @Test
    public void tilisiirtoOikeillaParametreillaKaksiSamaaTuotetta() {
        when(viite.uusi()).thenReturn(102);
        when(varasto.saldo(2)).thenReturn(100);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(102), eq("12345"), eq("33333-44455"), eq(14));
    }

    @Test
    public void tilisiirtoOikeillaParametreillaKaksiEriTuotettaToinenLoppu() {
        when(viite.uusi()).thenReturn(103);
        when(varasto.saldo(1)).thenReturn(0);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(100);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(103), eq("12345"), eq("33333-44455"), eq(7));
    }

    @Test
    public void aloitaAsiointiNollaaEdellisenOstoksenTiedot() {
        when(viite.uusi()).thenReturn(102);
        when(varasto.saldo(2)).thenReturn(100);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(102), eq("12345"), eq("33333-44455"), eq(7));
    }

    @Test
    public void uusiViitenumeroJokaiselleMaksuTapahtumalle() {
        when(viite.uusi()).thenReturn(1)
                .thenReturn(2)
                .thenReturn(3);

        when(varasto.saldo(2)).thenReturn(100)
                .thenReturn(99)
                .thenReturn(98);

        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(viite, times(1)).uusi();

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(viite, times(2)).uusi();
    }

    @Test
    public void poistaTuoteKorista() {
        when(viite.uusi()).thenReturn(105);
        when(varasto.saldo(2)).thenReturn(100);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "bisse", 7));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.lisaaKoriin(2);
        kauppa.poistaKorista(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(105), eq("12345"), eq("33333-44455"), eq(7));
    }

}
