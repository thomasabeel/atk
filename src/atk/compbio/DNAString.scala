package atk.compbio
/**
 * Efficient compact implementation to start DNA strings
 *
 * This implementation only supports ACGTN- in the DNA alphabet and will silently lose coding for other letters.
 *
 */
class DNAString(str: String) {

  val len = str.size
  def size = len
  def apply(i: Int) = get(i)

  private var hash: Long = 1
  override def hashCode() = hash.toInt
  def longHashCode=hash
  
  private val bytes = Array.ofDim[Byte]((str.length() + 1) / 2)

  for (i <- 0 until len) {

    set(i, str.charAt(i));
  }

  private def decode(b: Int): Char = b match {

    case 1 => 'A'
    case 2 => 'C'
    case 3 => 'G'
    case 4 => 'T'
    case 5 => 'N'
    case 6 => '-'
    case _ => '_'
  }

  private def encode(c: Char) = c match {

    case 'a' | 'A' =>
      1;
    case 'c' | 'C' =>
      2;
    case 'g' | 'G' =>
      3;
    case 't' | 'T' =>
      4;
    case 'n' | 'N' =>
      5;
    case '-' =>
      6;
    case _ =>
      0;
  }

  /* Zero based setter for the sequence */
  private def set(pos: Int, c: Char) {
    var coded = encode(c);
    var mask = 15
    if (pos % 2 == 1) {
      coded = coded << 4;

    } else
      mask <<= 4;

    var current = bytes(pos / 2).toInt;

    hash += current
    hash *= 8

    current &= mask;

    var newCurrent = current | coded;
    bytes(pos / 2) = newCurrent.toByte
  }

  /* Zero based getter for the sequence */

  def get(pos: Int): Char = {
    if (pos < 0 || pos >= len)
      throw new IndexOutOfBoundsException(pos + " is out of bounds")

    var current = bytes(pos / 2).toInt;
    var mask = 15;
    if (pos % 2 == 1) {
      current >>= 4;
    } else {
      current &= mask;
    }
    return decode(current);
  }

}