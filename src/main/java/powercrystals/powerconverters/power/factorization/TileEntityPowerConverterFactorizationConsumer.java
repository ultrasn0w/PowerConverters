package powercrystals.powerconverters.power.factorization;

import factorization.api.Charge;
import factorization.api.Coord;
import factorization.api.IChargeConductor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.powerconverters.mods.Factorization;
import powercrystals.powerconverters.position.BlockPosition;
import powercrystals.powerconverters.power.TileEntityEnergyConsumer;

public class TileEntityPowerConverterFactorizationConsumer extends TileEntityEnergyConsumer<IChargeConductor> implements IChargeConductor {
    private Charge _charge = new Charge(this);
    private int _chargeLastTick = 0;
    private static final int _maxCG = 1000;
    private boolean neighbourDirty = false;

    public TileEntityPowerConverterFactorizationConsumer() {
        super(Factorization.INSTANCE.powerSystem, 0, IChargeConductor.class);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) {
            return;
        }

        if (this._charge.getValue() < _maxCG) {
            this._charge.update();
        }

        if (this._charge.getValue() > 0) {
            int used = _charge.tryTake(_charge.getValue());
            _chargeLastTick = MathHelper.floor_float(used);
            storeEnergy((used * getPowerSystem().getInternalEnergyPerInput()), false);
        } else {
            this._chargeLastTick = 0;
        }
    }

    @Override
    public double getInputRate() {
        return this._chargeLastTick;
    }

    @Override
    public Charge getCharge() {
        return this._charge;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public Coord getCoord() {
        return new Coord(this);
    }

    @Override
    public void onNeighboorChanged() {
        super.onNeighboorChanged();

        try
        {
            Class fzNullClass = Class.forName("factorization.shared.TileEntityFzNull");
            for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity te = BlockPosition.getAdjacentTileEntity(this, d);
                if(te != null && fzNullClass.isAssignableFrom(te.getClass())) {
                    neighbourDirty = true;
                }
            }
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        if(neighbourDirty) {
            onNeighboorChanged();
            neighbourDirty = false;
        }
        return super.isConnected();
    }
}
