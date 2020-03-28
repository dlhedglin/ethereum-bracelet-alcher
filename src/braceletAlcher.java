import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.ExGrandExchange;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.listeners.SkillListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.event.types.SkillEvent;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;

@ScriptMeta(name = "Bracelet Alcher",  desc = "Script description", developer = "Developer's Name", category = ScriptCategory.MONEY_MAKING)
public class braceletAlcher extends Script implements SkillListener, RenderListener {
    private String REV_ETHER = "Revenant ether";
    private String UNCHARGED_BRACELET = "Bracelet of ethereum (uncharged)";
    private String CHARGED_BRACELET = "Bracelet of ethereum";
    private String NATURE_RUNE = "Nature rune";
    private boolean needEther = false;
    private boolean needBracelets =false;
    private int xpGained = 0;
    StopWatch timer;
    StopWatch priceCheckTimer;
    StopWatch buyTimeout;
    private static final String BASE_URL = "http://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";
    private static final int MILLION = 1000000;
    private static final int THOUSAND = 1000;
    private int profitPerAlch = 0;
    private int profit = 0;
    private int braceletPrice = 0;
    private boolean priceChecking = true;
    private int etherPrice;
    private int natPrice;


    @Override
    public void onStart()
    {
        //gui = new GUI(this);
        //gui.setVisible(true);
        timer = StopWatch.start();
        priceCheckTimer = StopWatch.start();
        etherPrice = getPrice(21820);
        natPrice = getPrice(561);
    }
    @Override
    public int loop() {
            if(braceletPrice != 0)
                profitPerAlch = 45000 - braceletPrice - natPrice - etherPrice;
            if (Inventory.getCount(UNCHARGED_BRACELET) > 0) {
                needBracelets = false;
                if(buyTimeout != null)
                    buyTimeout.reset();
            }
            if(buyTimeout != null)
            {
                if( buyTimeout.exceeds(Duration.ofSeconds(30)))
                    priceChecking = true;
            }
            if (Inventory.getCount(CHARGED_BRACELET) > 0 && Inventory.getCount(NATURE_RUNE) > 0) {
                Item chargedBrace = Inventory.getFirst(CHARGED_BRACELET);
                if (chargedBrace != null) {
                    Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, chargedBrace);
                    Time.sleepUntil(() -> Inventory.getCount(CHARGED_BRACELET) == 0, Random.low(666, 1222));
                }
            } else if (Inventory.getCount(NATURE_RUNE) == 0) {
                if (Bank.isOpen()) {
                    Bank.close();
                    Time.sleepUntil(() -> Bank.isClosed(), Random.low(666, 1222));
                } else if (!GrandExchange.isOpen()) {
                    Npc geClerk = Npcs.getNearest("Grand Exchange Clerk");
                    if (geClerk != null) {
                        geClerk.interact("Exchange");
                        Time.sleepUntil(() -> GrandExchange.isOpen(), Random.low(666, 1222));
                    } else
                        Log.info("Cant find a ge clerk");
                }
                if (GrandExchange.getFirst(a -> a.getItemDefinition().getName().equals(NATURE_RUNE) && a.getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)) != null) {
                    GrandExchange.collectAll();
                } else {
                    if (GrandExchangeSetup.isOpen()) {
                        GrandExchangeSetup.setItem(NATURE_RUNE);
                        GrandExchangeSetup.setQuantity(200);
                        GrandExchangeSetup.increasePrice(2);
                        GrandExchangeSetup.confirm();
                        Time.sleep(2222, 3333);
                        GrandExchange.collectAll();
                    } else
                        GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
                }

            }
            else if(priceChecking)
            {
                if(Bank.isOpen())
                {
                    Bank.close();
                }
                else if(!GrandExchange.isOpen())
                {
                    Npc geClerk = Npcs.getNearest("Grand Exchange Clerk");
                    if (geClerk != null)
                    {
                        geClerk.interact("Exchange");
                        Time.sleepUntil(() -> GrandExchange.isOpen(), Random.low(666, 1222));
                    }
                }
                else if(GrandExchange.getFirst(a-> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET)) != null)
                {
                    GrandExchange.getFirst(a-> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET)).abort();
                    GrandExchange.collectAll();
                }
                else if(Inventory.getCount(UNCHARGED_BRACELET) > 0)
                {
                    if(!GrandExchangeSetup.isOpen())
                    {
                        GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL);
                    }
                    else
                    {
                        GrandExchangeSetup.setItem(UNCHARGED_BRACELET);
                        GrandExchangeSetup.decreasePrice(2);
                        GrandExchangeSetup.setQuantity(1);
                        GrandExchangeSetup.confirm();
                        Time.sleepUntil(()-> GrandExchange.getFirst(a -> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET) && a.getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)) != null, Random.low(3000,5555));
                        braceletPrice = GrandExchange.getFirst(a -> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET) && a.getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)).getSpent() + 1;
                        GrandExchange.collectAll();
                        priceChecking = false;
                        priceCheckTimer.reset();
                    }

                }
                else if(!GrandExchangeSetup.isOpen())
                {
                    GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
                }
                else if(GrandExchangeSetup.isOpen())
                {
                    GrandExchangeSetup.setItem(UNCHARGED_BRACELET);
                    GrandExchangeSetup.increasePrice(2);
                    GrandExchangeSetup.confirm();
                    Time.sleepUntil(()-> GrandExchange.getFirst(a -> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET) && a.getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)) != null, Random.low(3000,5555));
                    GrandExchange.collectAll();
                    Time.sleepUntil(()-> Inventory.getCount(UNCHARGED_BRACELET) > 0, Random.low(5555,8888));
                }

            }
            else if (needBracelets) {
                if(priceCheckTimer.exceeds(Duration.ofMinutes(5)))
                {
                    priceChecking = true;
                }
                else if (Bank.isOpen()) {
                    Bank.close();
                    Time.sleepUntil(() -> Bank.isClosed(), Random.low(666, 1222));
                }
                else if (!GrandExchange.isOpen())
                {
                    Npc geClerk = Npcs.getNearest("Grand Exchange Clerk");
                    if (geClerk != null) {
                        geClerk.interact("Exchange");
                        Time.sleepUntil(() -> GrandExchange.isOpen(), Random.low(666, 1222));
                    }
                    else
                        Log.info("Cant find a ge clerk");
                }
                else if(GrandExchange.getFirst(a-> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET)) != null)
                {
                    RSGrandExchangeOffer foundOffer = GrandExchange.getFirst(a-> a.getItemDefinition().getName().equals(UNCHARGED_BRACELET));
                    if(!foundOffer.isEmpty())
                        GrandExchange.collectAll();
                }
                else {
                    if (GrandExchangeSetup.isOpen()) {
                        GrandExchangeSetup.setItem(UNCHARGED_BRACELET);
                        GrandExchangeSetup.setQuantity(Inventory.getFirst("Coins").getStackSize() / 45000);
                        GrandExchangeSetup.setPrice(braceletPrice);
                        GrandExchangeSetup.confirm();
                        Time.sleep(2222, 3333);
                        GrandExchange.collectAll();
                        Time.sleepUntil(() -> Inventory.getCount(UNCHARGED_BRACELET) > 0, Random.low(2222, 3333));
                        buyTimeout = StopWatch.start();
                    } else
                        GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
                }
            } else if (needEther) {
                if (Bank.isOpen()) {
                    Bank.close();
                    Time.sleepUntil(() -> Bank.isClosed(), Random.low(666, 1222));
                } else if (!GrandExchange.isOpen()) {
                    Npc geClerk = Npcs.getNearest("Grand Exchange Clerk");
                    if (geClerk != null) {
                        geClerk.interact("Exchange");
                        Time.sleepUntil(() -> GrandExchange.isOpen(), Random.low(666, 1222));
                    } else
                        Log.info("Cant find a ge clerk");
                } else if (GrandExchange.getFirst(a -> a.getItemDefinition().getName().equals(REV_ETHER) && a.getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)) != null) {
                    GrandExchange.collectAll();
                    needEther = false;
                } else {
                    if (GrandExchangeSetup.isOpen()) {
                        GrandExchangeSetup.setItem(REV_ETHER);
                        GrandExchangeSetup.setQuantity(200);
                        GrandExchangeSetup.increasePrice(2);
                        GrandExchangeSetup.confirm();
                        Time.sleep(2222, 3333);
                        GrandExchange.collectAll();
                        Time.sleepUntil(() -> Inventory.getCount(REV_ETHER) > 0, Random.low(2222, 3333));
                        needEther = false;
                    } else
                        GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
                }
            } else if (Inventory.getCount(REV_ETHER) > 0 && Inventory.getCount(UNCHARGED_BRACELET) > 0) {
                if (Inventory.getFirst(UNCHARGED_BRACELET).isNoted()) {
                    if (Bank.isClosed()) {
                        Bank.open(BankLocation.GRAND_EXCHANGE);
                        Time.sleepUntil(() -> Bank.isOpen(), Random.low(555, 1111));
                    } else {
                        Bank.depositAll(UNCHARGED_BRACELET);
                        Time.sleepUntil(() -> Inventory.getCount(UNCHARGED_BRACELET) == 0, Random.low(777, 2222));
                    }
                } else if (Inventory.getFirst(REV_ETHER).getStackSize() > 1) {
                    if (Bank.isClosed()) {
                        Bank.open(BankLocation.GRAND_EXCHANGE);
                        Time.sleepUntil(() -> Bank.isOpen(), Random.low(555, 1111));
                    } else {
                        Bank.depositAll(REV_ETHER);
                        Time.sleepUntil(() -> Inventory.getCount(REV_ETHER) == 0, Random.low(777, 2222));
                    }
                } else if (Bank.isOpen()) {
                    Bank.close();
                } else if (GrandExchange.isOpen()) {
                    Bank.open(BankLocation.GRAND_EXCHANGE);
                } else {
                    if (Inventory.getSelectedItem() == null) {
                        Inventory.getFirst(REV_ETHER).interact("Use");
                        Time.sleepUntil(() -> Inventory.getSelectedItem() != null, Random.low(555, 1111));
                    } else {
                        Inventory.getFirst(UNCHARGED_BRACELET).interact("Use");
                        Time.sleepUntil(() -> Inventory.getCount(CHARGED_BRACELET) > 0, Random.low(555, 1111));
                    }
                }

            } else if (Inventory.getCount(REV_ETHER) == 0) {
                if (Bank.isClosed()) {
                    Bank.open(BankLocation.GRAND_EXCHANGE);
                    Time.sleepUntil(() -> Bank.isMainTabOpen(), Random.low(2222, 3333));
                }
                else {
                    if (Bank.isOpen() && !Bank.contains(REV_ETHER)) {
                        if(!Time.sleepUntil(() -> Bank.getCount(REV_ETHER) > 0, 3000))
                        {
                            needEther = true;
                            Log.info("NEED ETHER NOW TRUE");
                        }
                    }
                    else {
                        Bank.withdraw(REV_ETHER, 1);
                        Time.sleepUntil(() -> Inventory.getCount(REV_ETHER) > 0, Random.low(1111, 2222));
                    }

                }
            } else if (Inventory.getCount(UNCHARGED_BRACELET) == 0) {
                if (Bank.isClosed()) {
                    Bank.open(BankLocation.GRAND_EXCHANGE);
                    Time.sleepUntil(() -> Bank.isMainTabOpen(), Random.low(2222, 3333));
                } else {
                    if (Bank.isOpen() && !Bank.contains(UNCHARGED_BRACELET) && Inventory.getCount(UNCHARGED_BRACELET) == 0 && Bank.getItems().length > 0) {
                        needBracelets = true;
                    } else {
                        Bank.withdraw(UNCHARGED_BRACELET, 25);
                        Time.sleepUntil(() -> Inventory.getCount(UNCHARGED_BRACELET) > 0, Random.low(1111, 2222));
                    }

                }
            }
        return Random.low(5,15);
    }

    @Override
    public void notify(SkillEvent skillEvent) {
        if(skillEvent.getType() == SkillEvent.TYPE_EXPERIENCE)
        {
            xpGained += skillEvent.getCurrent() - skillEvent.getPrevious();
            profit += profitPerAlch;
        }

    }

    @Override
    public void notify(RenderEvent renderEvent) {
        Graphics g = renderEvent.getSource();
        g.setColor(new Color(0,0,0,150));
        g.fillRoundRect(5,30,150,130,10,10);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.white);
        g.drawString("Runtime: " + timer.toElapsedString(), 10,50);
        g.drawString("Alchs: " + xpGained/65, 10,70);
        g.drawString("Alchs/hr: " + Math.floor(timer.getHourlyRate( xpGained/65)), 10,90);
        g.drawString("Profit: " + profit, 10,110);
        g.drawString("Profit/hr: " + Math.floor(timer.getHourlyRate(profit)), 10,130);
        g.drawString("Profit per Alch: " + profitPerAlch, 10,150);

    }
    int getPrice(final int id) {

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(BASE_URL + id).openStream()))) {

            final String raw = reader.readLine().replace(",", "").replace("\"", "").split("price:")[1].split("}")[0];

            return raw.endsWith("m") || raw.endsWith("k") ? (int) (Double.parseDouble(raw.substring(0, raw.length() - 1))

                    * (raw.endsWith("m") ? MILLION : THOUSAND)) : Integer.parseInt(raw);

        } catch (final Exception e) {

            e.printStackTrace();

        }

        return -1;

    }

}
