package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Arrays;
import java.util.List;

/**
 * Creates a custom fishHook for the Fisherman to throw
 * <p>
 * This class represents said entity
 */
public final class EntityFishHook extends Entity
{
    private static final int  TTL             = 360;
    private static final List possibleDrops_1 = Arrays.asList((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F),
                                                              new WeightedRandomFishable(new ItemStack(Items.leather), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.bone), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.potionitem), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.string), 5),
                                                              (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F),
                                                              new WeightedRandomFishable(new ItemStack(Items.bowl), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.stick), 5),
                                                              new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1),
                                                              new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List possibleDrops_2 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1),
                                                              new WeightedRandomFishable(new ItemStack(Items.name_tag), 1),
                                                              new WeightedRandomFishable(new ItemStack(Items.saddle), 1),
                                                              (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).func_150709_a(0.25F).func_150707_a(),
                                                              (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).func_150709_a(0.25F).func_150707_a(),
                                                              (new WeightedRandomFishable(new ItemStack(Items.book), 1)).func_150707_a());
    private static final List possibleDrops_3 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.func_150976_a()), 60),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13));
    public  EntityAIWorkFisherman fisherman;
    private int                   xTile;
    private int                   yTile;
    private int                   zTile;
    private Block                 inTile;
    private boolean               inGround;
    private int                   shake;
    private int                   hitBlock;
    private int                   hitWater;
    private int                   movedOnX;
    private int                   movedOnY;
    private int                   movedOnZ;
    private double                relativeRotation;
    //If the hook hits an entity it will stay with the entity
    private Entity                hitEntity;
    private int                   newPosRotationIncrements;
    private double                newX;
    private double                newY;
    private double                newZ;
    private double                newRotationYaw;
    private double                newRotationPitch;
    //Time at which the entity has been created
    private long                  creationTime;
    //Will be set true when the citizen caught a fish (to reset the fisherman)
    private boolean isCaughtFish = false;

    public EntityFishHook(World world, EntityAIWorkFisherman fisherman)
    {
        super(world);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.ignoreFrustumCheck = true;
        this.fisherman = fisherman;
        this.fisherman.setFishEntity(this);
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(fisherman.getCitizen().posX,
                                  fisherman.getCitizen().posY + 1.62 - (double) fisherman.getCitizen().yOffset,
                                  fisherman.getCitizen().posZ,
                                  fisherman.getCitizen().rotationYaw,
                                  fisherman.getCitizen().rotationPitch);
        this.posX -= Math.cos(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.posY -= 0.10000000149011612;
        this.posZ -= Math.sin(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        double f = 0.4;
        this.motionX = -Math.sin(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionZ = Math.cos(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionY = -Math.sin(this.rotationPitch / 180.0 * Math.PI) * f;
        this.setPosition(this.motionX, this.motionY, this.motionZ, 1.5, 1.0);
        this.creationTime = System.nanoTime();
    }

    private void setPosition(double x, double y, double z, double yaw, double pitch)
    {
        double squareRootXYZ = MathHelper.sqrt_double(x * x + y * y + z * z);
        double newX          = x / squareRootXYZ;
        double newY          = y / squareRootXYZ;
        double newZ          = z / squareRootXYZ;
        newX += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newY += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newZ += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newX *= yaw;
        newY *= yaw;
        newZ *= yaw;
        this.motionX = newX;
        this.motionY = newY;
        this.motionZ = newZ;
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(newX, newZ) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(newY, Math.sqrt(newX * newX + newZ * newZ)) * 180.0 / Math.PI);
        this.hitBlock = 0;
    }

    /**
     * Minecraft may call this method
     */
    @Override
    protected void entityInit(){}

    //Returns time to life of the entity
    public int getTtl()
    {
        return TTL;
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     *
     * @param range the real range
     * @return true or false
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double range)
    {
        double maxLength = this.boundingBox.getAverageEdgeLength() * 4.0;
        maxLength *= 64.0;
        return range < maxLength * maxLength;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (this.newPosRotationIncrements > 0)
        {
            double x           = this.posX + (this.newX - this.posX) / (double) this.newPosRotationIncrements;
            double y           = this.posY + (this.newY - this.posY) / (double) this.newPosRotationIncrements;
            double z           = this.posZ + (this.newZ - this.posZ) / (double) this.newPosRotationIncrements;
            double newRotation = MathHelper.wrapAngleTo180_double(this.newRotationYaw - (double) this.rotationYaw);
            this.rotationYaw = (float) ((double) this.rotationYaw + newRotation / (double) this.newPosRotationIncrements);
            this.rotationPitch = (float) ((double) this.rotationPitch + (this.newRotationPitch - (double) this.rotationPitch) / (double) this.newPosRotationIncrements);
            --this.newPosRotationIncrements;
            this.setPosition(x, y, z);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                if (this.fisherman == null)
                {
                    return;
                }

                ItemStack itemstack = this.fisherman.getCitizen().getInventory().getHeldItem();

                if (this.fisherman.getCitizen().isDead
                    || !this.fisherman.getCitizen().isEntityAlive()
                    || itemstack == null
                    || !itemstack.getItem().equals(Items.fishing_rod)
                    || this.getDistanceSqToEntity(this.fisherman.getCitizen()) > 1024.0D)
                {
                    this.setDead();
                    this.fisherman.setFishEntity(null);
                    return;
                }

                if (this.hitEntity != null)
                {
                    if (!this.hitEntity.isDead)
                    {
                        this.posX = this.hitEntity.posX;
                        this.posY = this.hitEntity.boundingBox.minY + (double) this.hitEntity.height * 0.8D;
                        this.posZ = this.hitEntity.posZ;
                        return;
                    }

                    this.hitEntity = null;
                }
            }

            if (this.shake > 0)
            {
                --this.shake;
            }

            if (this.inGround)
            {
                if (this.worldObj.getBlock(this.xTile, this.yTile, this.zTile) == this.inTile)
                {
                    ++this.hitBlock;

                    if (this.hitBlock == 1200)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= (this.rand.nextDouble() * 0.2);
                this.motionY *= (this.rand.nextDouble() * 0.2);
                this.motionZ *= (this.rand.nextDouble() * 0.2);
                this.hitBlock = 0;
                this.hitWater = 0;
            }
            else
            {
                ++this.hitWater;
            }

            Vec3                 vec31                = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3                 vec3                 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                                                                           this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            double initialDistance = 0.0;
            double currentDistance;

            for (Object aList : list)
            {
                Entity entity1 = (Entity) aList;

                if (entity1.canBeCollidedWith() && this.fisherman != null && (!entity1.equals(this.fisherman.getCitizen()) || this.hitWater >= 5))
                {
                    double               f                     = 0.3;
                    AxisAlignedBB        axisAlignedBB         = entity1.boundingBox.expand(f, f, f);
                    MovingObjectPosition movingObjectPosition1 = axisAlignedBB.calculateIntercept(vec31, vec3);

                    if (movingObjectPosition1 != null)
                    {
                        currentDistance = vec31.distanceTo(movingObjectPosition1.hitVec);

                        if (currentDistance < initialDistance || Math.abs(initialDistance) <= 0.001)

                        {
                            entity = entity1;
                            initialDistance = currentDistance;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.fisherman.getCitizen()), 0.0F))
                    {
                        this.hitEntity = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    this.inGround = true;
                }
            }

            if (!this.inGround)
            {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                double motion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float) (Math.atan2(this.motionY, this.motionZ) * 180.0 / Math.PI);
                this.rotationPitch = (float) (Math.atan2(this.motionY, motion) * 180.0 / Math.PI);
                while ((double) this.rotationPitch - (double) this.prevRotationPitch < -180.0)
                {
                    this.prevRotationPitch -= 360.0;
                }

                while ((double) this.rotationPitch - (double) this.prevRotationPitch >= 180.0)
                {
                    this.prevRotationPitch += 360.0;
                }

                while ((double) this.rotationYaw - (double) this.prevRotationYaw < -180.0)
                {
                    this.prevRotationYaw -= 360.0;
                }

                while ((double) this.rotationYaw - (double) this.prevRotationYaw >= 180.0)
                {
                    this.prevRotationYaw += 360.0;
                }

                this.rotationPitch =
                        (float) ((double) this.prevRotationPitch + ((double) this.rotationPitch - (double) this.prevRotationPitch) * 0.2D);
                this.rotationYaw =
                        (float) ((double) this.prevRotationYaw + ((double) this.rotationYaw - (double) this.prevRotationYaw) * 0.2D);
                double f6 = 0.92F;

                if (this.onGround || this.isCollidedHorizontally)
                {
                    f6 = 0.5;
                }

                byte   b0  = 5;
                double d10 = 0.0;

                for (int j = 0; j < b0; ++j)
                {
                    double        d3             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * j / b0 - 0.125 + 0.125;
                    double        d4             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (j + 1) / b0 - 0.125 + 0.125;
                    AxisAlignedBB axisAlignedBB1 = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d3, this.boundingBox.minZ, this.boundingBox.maxX, d4, this.boundingBox.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisAlignedBB1, Material.water))
                    {
                        d10 += 1.0 / b0;
                    }
                }

                if (!this.worldObj.isRemote && d10 > 0.0)
                {
                    WorldServer worldserver = (WorldServer) this.worldObj;
                    int         k           = 1;

                    if (this.rand.nextDouble() < 0.25 && this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX),
                                                                                            MathHelper.floor_double(this.posY) + 1,
                                                                                            MathHelper.floor_double(this.posZ)))
                    {
                        k = 2;
                    }

                    if (this.rand.nextDouble() < 0.5 && !this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX),
                                                                                         MathHelper.floor_double(this.posY) + 1,
                                                                                         MathHelper.floor_double(this.posZ)))
                    {
                        --k;
                    }

                    if (this.movedOnX > 0)
                    {
                        --this.movedOnX;

                        if (this.movedOnX <= 0)
                        {
                            this.movedOnY = 0;
                            this.movedOnZ = 0;
                        }
                    }
                    else
                    {
                        double bubbleY;
                        double bubbleZ;
                        double bubbleX;

                        double cosYPosition;
                        double increasedYPosition;
                        double sinYPosition;

                        if (this.movedOnZ > 0)
                        {
                            this.movedOnZ -= k;

                            if (this.movedOnZ <= 0)
                            {
                                this.motionY -= 0.20000000298023224D;
                                this.playSound("random.splash", 0.25F,
                                               (float) (1.0D + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.4D));
                                bubbleY = Math.floor(this.boundingBox.minY);
                                worldserver.func_147487_a("bubble",
                                                          this.posX,
                                                          (bubbleY + 1.0),
                                                          this.posZ,
                                                          (int) (1.0 + this.width * 20.0),
                                                          (double) this.width,
                                                          0.0,
                                                          (double) this.width,
                                                          0.20000000298023224);
                                worldserver.func_147487_a("wake",
                                                          this.posX,
                                                          (bubbleY + 1.0),
                                                          this.posZ,
                                                          (int) (1.0 + this.width * 20.0),
                                                          (double) this.width,
                                                          0.0,
                                                          (double) this.width,
                                                          0.20000000298023224);
                                this.movedOnX = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                                isCaughtFish = true;
                            }
                            else
                            {
                                this.relativeRotation = this.relativeRotation + this.rand.nextGaussian() * 4.0;
                                bubbleY = this.relativeRotation * 0.017453292;
                                sinYPosition = Math.sin(bubbleY);
                                cosYPosition = Math.cos(bubbleY);
                                bubbleX = this.posX + (sinYPosition * this.movedOnZ * 0.1);
                                increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
                                bubbleZ = this.posZ + (cosYPosition * this.movedOnZ * 0.1);

                                if (this.rand.nextDouble() < 0.15)
                                {
                                    worldserver.func_147487_a("bubble", bubbleX, increasedYPosition - 0.10000000149011612, bubbleZ, 1, sinYPosition, 0.1D, cosYPosition, 0.0);
                                }

                                double f3 = sinYPosition * 0.04;
                                double f4 = cosYPosition * 0.04;
                                worldserver.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, f4, 0.01, (-f3), 1.0);
                                worldserver.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, (-f4), 0.01, f3, 1.0);

                            }
                        }
                        else if (this.movedOnY > 0)
                        {
                            this.movedOnY -= k;
                            bubbleY = 0.15;

                            if (this.movedOnY < 20)
                            {
                                bubbleY = bubbleY + (double) (20 - this.movedOnY) * 0.05;
                            }
                            else if (this.movedOnY < 40)
                            {
                                bubbleY = bubbleY + (double) (40 - this.movedOnY) * 0.02;
                            }
                            else if (this.movedOnY < 60)
                            {
                                bubbleY = bubbleY + (double) (60 - this.movedOnY) * 0.01;
                            }

                            if (this.rand.nextDouble() < bubbleY)
                            {
                                sinYPosition = (double) MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292D;
                                cosYPosition = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                                bubbleX = this.posX + (Math.sin(sinYPosition) * cosYPosition * 0.1);
                                increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
                                bubbleZ = this.posZ + (Math.cos(sinYPosition) * cosYPosition * 0.1);
                                worldserver.func_147487_a("splash",
                                                          bubbleX,
                                                          increasedYPosition,
                                                          bubbleZ,
                                                          2 + this.rand.nextInt(2),
                                                          0.10000000149011612,
                                                          0.0,
                                                          0.10000000149011612,
                                                          0.0);

                            }

                            if (this.movedOnY <= 0)
                            {
                                this.relativeRotation = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                                this.movedOnZ = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                            }
                        }
                        else
                        {
                            this.movedOnY = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                            this.movedOnY -= EnchantmentHelper.func_151387_h(this.fisherman.getCitizen()) * 20 * 5;
                        }
                    }

                    if (this.movedOnX > 0)
                    {
                        this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * 0.2;
                    }
                }

                currentDistance = d10 * 2.0D - 1.0;
                this.motionY += 0.03999999910593033 * currentDistance;

                if (d10 > 0.0)
                {
                    f6 = f6 * 0.9;
                    this.motionY *= 0.8;
                }

                this.motionX *= f6;
                this.motionY *= f6;
                this.motionZ *= f6;
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead()
    {
        super.setDead();

        if (this.fisherman != null)
        {
            this.fisherman.setFishEntity(null);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setShort("xTile", (short) this.xTile);
        p_70014_1_.setShort("yTile", (short) this.yTile);
        p_70014_1_.setShort("zTile", (short) this.zTile);
        p_70014_1_.setByte("inTile", (byte) Block.getIdFromBlock(this.inTile));
        p_70014_1_.setByte("shake", (byte) this.shake);
        p_70014_1_.setByte("inGround", (byte) (this.inGround ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        if (!fisherman.getFishEntity().equals(this))
        {
            this.setDead();
        }
        else
        {
            this.xTile = p_70037_1_.getShort("xTile");
            this.yTile = p_70037_1_.getShort("yTile");
            this.zTile = p_70037_1_.getShort("zTile");
            this.inTile = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
            this.shake = p_70037_1_.getByte("shake") & 255;
            this.inGround = p_70037_1_.getByte("inGround") == 1;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return !(o == null || getClass() != o.getClass() || !super.equals(o));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public int getDamage()
    {
        double citizenPosX = this.fisherman.getCitizen().posX;
        double citizenPosY = this.fisherman.getCitizen().posY;
        double citizenPosZ = this.fisherman.getCitizen().posZ;

        if (this.worldObj.isRemote)
        {
            return 0;
        }
        else
        {
            byte itemDamage = 0;

            if (this.hitEntity != null)
            {
                double x = citizenPosX - this.posX;
                double y = citizenPosY - this.posY;
                double z = citizenPosZ - this.posZ;

                this.hitEntity.motionX += x * 0.1;
                this.hitEntity.motionY += y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08;
                this.hitEntity.motionZ += z * 0.1;
                itemDamage = 3;
            }
            else if (this.movedOnX > 0)
            {
                EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingLoot());
                double     distanceX  = citizenPosX - this.posX;
                double     distanceY  = citizenPosY - this.posY;
                double     distanceZ  = citizenPosZ - this.posZ;

                entityitem.motionX = distanceX * 0.1;
                entityitem.motionY = distanceY * 0.1 + Math.sqrt(Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ)) * 0.08;
                entityitem.motionZ = distanceZ * 0.1;
                this.worldObj.spawnEntityInWorld(entityitem);
                this.fisherman.getCitizen().worldObj.spawnEntityInWorld(new EntityXPOrb(this.fisherman.getCitizen().worldObj,
                                                                                        citizenPosX,
                                                                                        citizenPosY + 0.D,
                                                                                        citizenPosZ + 0.5,
                                                                                        this.rand.nextInt(6) + 1));
                itemDamage = 1;
            }

            if (this.inGround)
            {
                itemDamage = 2;
            }

            this.setDead();
            this.fisherman.setFishEntity(null);
            return itemDamage;
        }
    }

    private ItemStack getFishingLoot()
    {
        double random                  = this.worldObj.rand.nextDouble();
        int    fishingSpeedEnchantment = EnchantmentHelper.func_151386_g(this.fisherman.getCitizen());
        int    fishingLootEnchantment  = EnchantmentHelper.func_151387_h(this.fisherman.getCitizen());
        double speedBonus              = 0.1 - fishingSpeedEnchantment * 0.025 - fishingLootEnchantment * 0.01;
        double lootBonus               = 0.05 + fishingSpeedEnchantment * 0.01 - fishingLootEnchantment * 0.01;
        //clamp_float gives the values an upper limit
        speedBonus = MathHelper.clamp_float((float) speedBonus, 0.0F, 1.0F);
        lootBonus = MathHelper.clamp_float((float) lootBonus, 0.0F, 1.0F);
        int buildingLevel = fisherman.getCitizen().getWorkBuilding().getBuildingLevel();

        if (random < speedBonus || buildingLevel == 1)
        {
            return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_3)).func_150708_a(this.rand);
        }
        else
        {
            random -= speedBonus;

            if (random < lootBonus || buildingLevel == 2)
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_1)).func_150708_a(this.rand);
            }
            else
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_2)).func_150708_a(this.rand);
            }
        }
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (fisherman != null ? fisherman.hashCode() : 0);
        return result;
    }

    public long getCreationTime()
    {
        return creationTime;
    }

    public boolean hasHitEntity()
    {
        return hitEntity != null;
    }

    public boolean caughtFish()
    {
        return isCaughtFish;
    }

    public void setCaughtFish(boolean caughtFish)
    {
        isCaughtFish = caughtFish;
    }
}
