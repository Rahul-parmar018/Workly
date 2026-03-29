import Provider from '../models/Provider.js';
import RazorpayService from '../services/RazorpayService.js';

class ProviderController {
  /**
   * Onboard Provider with Razorpay Route
   */
  async onboardProvider(req, res) {
    const { providerId, email, businessName, accountType } = req.body;

    try {
      const provider = await Provider.findById(providerId);
      if (!provider) return res.status(404).json({ error: 'Provider not found' });

      // Create Razorpay Linked Account
      const account = await RazorpayService.razorpay.accounts.create({
        type: 'route',
        email: email,
        profile: {
          category: 'service_marketplace',
          addresses: {
             // Mock details for example
          }
        }
      });

      provider.razorpayAccountId = account.id;
      provider.kycStatus = 'pending';
      await provider.save();

      res.status(201).json({ success: true, accountId: account.id });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
}

export default new ProviderController();
