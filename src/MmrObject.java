
/**Object for holding MMR related stuff
 * @author epsilon
 * @see StatsObject
 */
public class MmrObject
{
    private double mmr;

    MmrObject()
    {
        mmr = -1;
    }
    
    /**Getter for MMR as a double
     * @return MMR as a double
     */
    public double getMmr()
    {
        return mmr;
    }

    /**Setter for MMR
     * @param mmr the MMR to set
     */
    public void setmmr(Double mmr)
    {
        this.mmr = mmr;
    }
}
