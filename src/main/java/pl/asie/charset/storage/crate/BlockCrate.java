package pl.asie.charset.storage.crate;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.GenericExtendedProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockCrate extends BlockBase implements ITileEntityProvider {
    public static final boolean SHOW_ALL_CRATES = ModCharsetLib.INDEV;
    public static final GenericExtendedProperty<CrateCacheInfo> PROPERTY = new GenericExtendedProperty<CrateCacheInfo>("cache", CrateCacheInfo.class);

    public BlockCrate() {
        super(Material.WOOD);
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.crate");
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCrate) {
            return ((TileEntityCrate) tile).getFlamability();
        }

        return 0;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCrate) {
            ((TileEntityCrate) tile).neighborChanged(block);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState.Builder(this).add(PROPERTY).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCrate) {
            return ((IExtendedBlockState) state).withProperty(PROPERTY, ((TileEntityCrate) tile).getCacheInfo());
        } else {
            return state;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCrate();
    }

    @Override
    public void getSubBlocks(Item me, CreativeTabs tab, NonNullList<ItemStack> itemList) {
        if (todaysCrates == null) {
            todaysCrates = new ArrayList<ItemStack>();

            Calendar cal = ModCharsetLib.calendar.get();
            int doy = cal.get(Calendar.DAY_OF_YEAR) - 1 /* start at 0, not 1 */;

            ArrayList<ItemStack> weeklyCrates = new ArrayList<>();
            weeklyCrates.addAll(CrateRegistry.INSTANCE.getCrates());
            if (!SHOW_ALL_CRATES) {
                Collections.shuffle(weeklyCrates, new Random(doy));
            }

            int cratesToAdd = 1;

            for (ItemStack crate : weeklyCrates) {
                todaysCrates.add(crate);
                cratesToAdd--;
                if (!SHOW_ALL_CRATES && cratesToAdd <= 0) {
                    break;
                }
            }
        }

        itemList.addAll(todaysCrates);
    }

    ArrayList<ItemStack> todaysCrates = null;

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }
}
