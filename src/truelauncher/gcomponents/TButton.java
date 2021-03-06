/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package truelauncher.gcomponents;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ButtonModel;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class TButton extends JButton {

	public TButton() {

	}

	private boolean renderImage = false;
	private Image bgimage;
	private Image bgimage_pressed;
	private Image bgimage_focus;

	/**
	 * WARNING: if you want to use custom image you should also add files for
	 * focus and pressed state. See close and hide buttons in the images folder
	 * for example.
	 *
	 * @param filepath - path to the image in normal state
	 */
	public void setBackgroundImage(InputStream is_n, InputStream is_pr, InputStream is_f) {
		try {
			renderImage = true;
			setOpaque(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(null);
			bgimage = ImageIO.read(is_n);
			bgimage = bgimage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
			bgimage_pressed = ImageIO.read(is_pr);
			bgimage_pressed = bgimage_pressed.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
			bgimage_focus = ImageIO.read(is_f);
			bgimage_focus = bgimage_focus.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		if (renderImage) {
			ButtonModel buttonModel = getModel();
			Graphics2D g = (Graphics2D) graphics;
			if (!isEnabled()) {
				g.drawImage(bgimage, 0, 0, getWidth(), getHeight(), null);
			} else if (buttonModel.isRollover()) {
				if (buttonModel.isPressed()) {
					g.drawImage(bgimage_pressed, 0, 0, getWidth(), getHeight(), null);
				} else {
					g.drawImage(bgimage_focus, 0, 0, getWidth(), getHeight(), null);
				}
			} else {
				g.drawImage(bgimage, 0, 0, getWidth(), getHeight(), null);
			}
			g.finalize();
		}
		super.paintComponent(graphics);
	}

}
