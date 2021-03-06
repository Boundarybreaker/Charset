/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.ItemUtils;

public class BlockTank extends BlockBase implements ITileEntityProvider {
    public static final int VARIANTS = 18;
    protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.05f, 0, 0.05f, 0.95f, 1, 0.95f);
    private static final PropertyInteger VARIANT = PropertyInteger.create("connections", 0, 11);

    public BlockTank() {
        super(Material.GLASS);
        setHardness(0.6F);
        setHarvestLevel("pickaxe", 0);
        setUnlocalizedName("charset.tank");
        setFullCube(false);
        setOpaqueCube(false);
        setComparatorInputOverride(true);
        setSoundType(SoundType.GLASS);
    }

    protected int getVariant(IBlockAccess access, BlockPos pos) {
        IBlockState state = access.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockTank)) {
            return -1;
        }

        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileTank) {
            return ((TileTank) tile).getVariant();
        } else {
            return 0;
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        Item item = Item.getItemFromBlock(this);

        for (int i = 0; i <= 17; i++) {
            ItemStack stack = new ItemStack(item);
            ItemUtils.getTagCompound(stack, true).setInteger("color", i - 1);
            items.add(stack);
        }
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        switch (side) {
            case DOWN:
                if (getVariant(blockAccess, pos) == getVariant(blockAccess, pos.down())) {
                    return false;
                }
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            case UP:
                if (getVariant(blockAccess, pos) == getVariant(blockAccess, pos.up())) {
                    return false;
                }
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            default:
                return true;
        }
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tankEntity = worldIn.getTileEntity(pos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            FluidStack contents = tank.getContents();
            if (contents != null) {
                return this.blockHardness * (2.5f + (contents.amount * 5.0f / ((TileTank) tankEntity).getCapacity()));
            } else {
                return this.blockHardness;
            }
        } else {
            return this.blockHardness;
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int variant = getVariant(worldIn, pos);
        int variantUp = getVariant(worldIn, pos.up());
        int variantDown = getVariant(worldIn, pos.down());

        return state.withProperty(VARIANT,
                ((variantUp == variant) ? 0 : 2)
                | ((variantDown == variant) ? 0 : 1)
                | (variant > 0 ? (variant == 17 ? 8 : 4) : 0));
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        BlockPos tankPos = pos;
        TileEntity tankEntity = worldIn.getTileEntity(tankPos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            if (tank.fluidStack != null && tank.fluidStack.amount >= 1000) {
                float chance = (float) (tank.fluidStack.amount) / (TileTank.CAPACITY / 2);
                if (worldIn.rand.nextFloat() <= chance) {
                    worldIn.setBlockState(pos, tank.fluidStack.getFluid().getBlock().getDefaultState());
                }
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND)
            return false;

        ItemStack stack = playerIn.getHeldItem(hand);
        IFluidHandlerItem handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, stack, null);
        if (handler != null) {
            TileEntity tile = worldIn.getTileEntity(pos);

            if (tile instanceof TileTank) {
                TileTank tank = (TileTank) tile;
                if (!worldIn.isRemote) {
                    boolean changed = false;

                    FluidStack fluidContained = tank.getBottomTank().fluidStack;
                    FluidStack fluidExtracted;
                    if (fluidContained != null) {
                        FluidStack f = fluidContained.copy();
                        f.amount = Fluid.BUCKET_VOLUME;
                        fluidExtracted = handler.drain(f, false);
                    } else {
                        fluidExtracted = handler.drain(Fluid.BUCKET_VOLUME, false);
                    }

                    if (fluidExtracted == null) {
                        // tank -> holder
                        fluidExtracted = tank.drain(Fluid.BUCKET_VOLUME, false, false);
                        if (fluidExtracted != null) {
                            int amount = handler.fill(fluidExtracted, false);
                            if (amount > 0) {
                                fluidExtracted.amount = amount;
                                fluidExtracted = tank.drain(fluidExtracted, true, false);
                                if (fluidExtracted != null) {
                                    handler.fill(fluidExtracted, true);
                                    changed = true;
                                }
                            }
                        }
                    } else {
                        // holder -> tank
                        int amount = tank.fill(fluidExtracted, false);
                        if (amount > 0) {
                            fluidExtracted.amount = amount;
                            fluidExtracted = handler.drain(fluidExtracted, !playerIn.isCreative());
                            if (fluidExtracted != null) {
                                tank.fill(fluidExtracted, true);
                                changed = true;
                            }
                        }
                    }

                    if (changed) {
                        playerIn.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, handler.getContainer());
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        // People may not place an empty tank between two tanks which differ only in liquid
        return !TileTank.checkPlacementConflict(worldIn.getTileEntity(pos.down()), worldIn.getTileEntity(pos.up()), -1);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        // Tank connection refresh logic
        if (blockIn == this || worldIn.isAirBlock(fromPos)) {
            BlockPos tankPos = pos;
            TileEntity tankEntity = worldIn.getTileEntity(tankPos);
            if (tankEntity instanceof TileTank) {
                ((TileTank) tankEntity).onTankStructureChanged();
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTank();
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, net.minecraft.item.EnumDyeColor color) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTank) {
            if (((TileTank) tile).setVariant(color.getMetadata() + 1)) {
                return true;
            }
        }

        return false;
    }
}
